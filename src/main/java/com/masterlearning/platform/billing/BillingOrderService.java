package com.masterlearning.platform.billing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterlearning.platform.subscription.SubscriptionPlan;
import com.masterlearning.platform.subscription.SubscriptionPlanRepository;
import com.masterlearning.platform.subscription.UserSubscriptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class BillingOrderService {
    private final BillingOrderRepository orders;
    private final BillingPaymentRepository payments;
    private final SubscriptionPlanRepository plans;
    private final UserSubscriptionService subscriptions;
    private final ObjectMapper mapper;
    private final String keyId;
    private final String keySecret;
    private final String webhookSecret;
    private final RestClient razorpay;

    public BillingOrderService(BillingOrderRepository orders, BillingPaymentRepository payments,
            SubscriptionPlanRepository plans, UserSubscriptionService subscriptions, ObjectMapper mapper,
            @Value("${app.payment.razorpay.key-id:}") String keyId,
            @Value("${app.payment.razorpay.key-secret:}") String keySecret,
            @Value("${app.payment.razorpay.webhook-secret:}") String webhookSecret,
            @Value("${app.payment.razorpay.base-url:https://api.razorpay.com/v1}") String baseUrl) {
        this.orders = orders; this.payments = payments; this.plans = plans; this.subscriptions = subscriptions;
        this.mapper = mapper; this.keyId = keyId == null ? "" : keyId.trim(); this.keySecret = keySecret == null ? "" : keySecret.trim(); this.webhookSecret = webhookSecret == null ? "" : webhookSecret.trim();
        this.razorpay = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Transactional
    public BillingOrderResponse create(UUID userId, CreateBillingOrderRequest request) {
        if (request == null || request.planCode() == null || request.planCode().isBlank()) throw new IllegalArgumentException("Plan code is required");
        var plan = plans.findByCode(request.planCode().trim().toUpperCase()).filter(SubscriptionPlan::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Active subscription plan not found"));
        String cycle = request.billingCycle() == null ? "MONTHLY" : request.billingCycle().trim().toUpperCase();
        if (!cycle.equals("MONTHLY") && !cycle.equals("YEARLY")) throw new IllegalArgumentException("Billing cycle must be MONTHLY or YEARLY");
        BigDecimal amount = cycle.equals("YEARLY") ? plan.getYearlyPrice() : plan.getMonthlyPrice();
        if (subscriptions.hasCurrentPaidSubscription(userId)) {
            throw new IllegalStateException("User already has a current paid subscription");
        }
        if (amount.signum() <= 0) throw new IllegalArgumentException("Use the free-plan registration flow for a zero-price plan");

        var order = orders.save(new BillingOrder(UUID.randomUUID(), userId, plan.getId(), cycle, amount, plan.getCurrency()));
        String gatewayOrderId = createRazorpayOrder(order, plan);
        order.setExternalOrderId(gatewayOrderId); order.markPendingPayment(); orders.save(order);
        payments.save(new BillingPayment(UUID.randomUUID(), order.getId(), "RAZORPAY", amount, plan.getCurrency()));
        subscriptions.createPending(userId, plan.getCode(), cycle);

        return new BillingOrderResponse(order.getId(), plan.getCode(), plan.getName(), cycle, amount, plan.getCurrency(), order.getStatus(), "RAZORPAY", order.getExternalOrderId(), keyId);
    }

    @Transactional
    public void verifyPayment(UUID userId, String internalOrderId, String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        var order = orders.findById(UUID.fromString(internalOrderId)).orElseThrow(() -> new IllegalArgumentException("Billing order not found"));
        if (!order.getUserId().equals(userId)) throw new SecurityException("Billing order does not belong to current user");
        if ("PAID".equals(order.getStatus())) return;
        if (!razorpayOrderId.equals(order.getExternalOrderId())) throw new SecurityException("Gateway order mismatch");
        String expected = hmacHex(order.getExternalOrderId() + "|" + razorpayPaymentId, keySecret);
        if (razorpaySignature == null || !java.security.MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), razorpaySignature.getBytes(StandardCharsets.UTF_8)))
            throw new SecurityException("Invalid payment signature");

        var payment = payments.findAll().stream().filter(item -> item.getOrderId().equals(order.getId())).findFirst()
                .orElseThrow(() -> new IllegalStateException("Payment record not found"));
        payment.capture(razorpayPaymentId); payments.save(payment); order.markPaid(); orders.save(order);
        var start = LocalDate.now();
        var end = order.getBillingCycle().equals("YEARLY") ? start.plusYears(1).minusDays(1) : start.plusMonths(1).minusDays(1);
        var plan = plans.findById(order.getPlanId()).orElseThrow(() -> new IllegalStateException("Subscription plan not found"));
        subscriptions.activatePending(userId, plan.getCode(), order.getBillingCycle(), start, end, razorpayPaymentId);
    }

    @Transactional
    public void handleRazorpayWebhook(String rawBody, String signature) {
        String expected = hmacHex(rawBody, webhookSecret);
        if (signature == null || !java.security.MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8))) {
            throw new SecurityException("Invalid Razorpay webhook signature");
        }

        try {
            JsonNode root = mapper.readTree(rawBody);
            String event = root.path("event").asText();
            if (!"order.paid".equals(event) && !"payment.captured".equals(event)) {
                return;
            }

            JsonNode paymentEntity = root.path("payload").path("payment").path("entity");
            String paymentId = paymentEntity.path("id").asText();
            String gatewayOrderId = paymentEntity.path("order_id").asText();

            if (gatewayOrderId.isBlank()) {
                gatewayOrderId = root.path("payload").path("order").path("entity").path("id").asText();
            }

            var order = orders.findByExternalOrderId(gatewayOrderId)
                    .orElseThrow(() -> new IllegalArgumentException("Billing order not found for Razorpay order"));

            if ("PAID".equals(order.getStatus())) {
                return;
            }

            var payment = payments.findAll().stream()
                    .filter(item -> item.getOrderId().equals(order.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Payment record not found"));

            payment.capture(paymentId);
            payments.save(payment);
            order.markPaid();
            orders.save(order);

            var start = LocalDate.now();
            var end = order.getBillingCycle().equals("YEARLY")
                    ? start.plusYears(1).minusDays(1)
                    : start.plusMonths(1).minusDays(1);
            var plan = plans.findById(order.getPlanId())
                    .orElseThrow(() -> new IllegalStateException("Subscription plan not found"));

            subscriptions.activatePending(
                    order.getUserId(), plan.getCode(), order.getBillingCycle(), start, end, paymentId
            );
        } catch (Exception ex) {
            if (ex instanceof SecurityException || ex instanceof IllegalArgumentException) throw ex;
            throw new IllegalStateException("Unable to process Razorpay webhook", ex);
        }
    }

    private String createRazorpayOrder(BillingOrder order, SubscriptionPlan plan) {
        if (keyId.isBlank() || keySecret.isBlank()) throw new IllegalStateException("Razorpay is not configured. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET.");
        long amountPaise = order.getAmount().movePointRight(2).longValueExact();
        var body = new java.util.LinkedHashMap<String, Object>(); body.put("amount", amountPaise); body.put("currency", plan.getCurrency()); body.put("receipt", order.getId().toString());
        String raw = razorpay.post().uri("/orders").contentType(MediaType.APPLICATION_JSON).headers(h -> h.setBasicAuth(keyId, keySecret)).body(body).retrieve().body(String.class);
        try { JsonNode node = mapper.readTree(raw); String id = node.path("id").asText(); if (id.isBlank()) throw new IllegalStateException("Razorpay did not return an order id"); return id; }
        catch (Exception ex) { throw new IllegalStateException("Unable to create Razorpay order", ex); }
    }

    private String hmacHex(String payload, String secret) {
        if (secret == null || secret.isBlank()) throw new IllegalStateException("Razorpay secret is not configured");
        try { Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")); return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception ex) { throw new IllegalStateException("Unable to generate payment signature", ex); }
    }
}