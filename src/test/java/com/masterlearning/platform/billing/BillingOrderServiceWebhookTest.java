package com.masterlearning.platform.billing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterlearning.platform.subscription.SubscriptionPlanRepository;
import com.masterlearning.platform.subscription.UserSubscriptionService;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class BillingOrderServiceWebhookTest {
    private static final String SECRET = "webhook-test-secret";

    @Test
    void processedEventWithIdenticalPayloadIsIgnoredAsRetry() throws Exception {
        String body = validPayload();
        BillingWebhookEvent existing = mock(BillingWebhookEvent.class);
        when(existing.getPayloadHash()).thenReturn(hash(body));
        when(existing.getStatus()).thenReturn("PROCESSED");
        BillingWebhookEventRepository events = mock(BillingWebhookEventRepository.class);
        when(events.findByProviderAndEventId("RAZORPAY", "evt_test_123"))
                .thenReturn(Optional.of(existing));

        BillingOrderService service = service(events);

        assertDoesNotThrow(() -> service.handleRazorpayWebhook(body, signature(body)));
        verifyNoInteractionsExceptLookup(events);
    }

    @Test
    void rejectsReusedEventIdWhenPayloadHasChanged() throws Exception {
        String body = validPayload();
        BillingWebhookEvent existing = mock(BillingWebhookEvent.class);
        when(existing.getPayloadHash()).thenReturn("different-payload-hash");
        when(existing.getStatus()).thenReturn("PROCESSED");
        BillingWebhookEventRepository events = mock(BillingWebhookEventRepository.class);
        when(events.findByProviderAndEventId("RAZORPAY", "evt_test_123"))
                .thenReturn(Optional.of(existing));

        BillingOrderService service = service(events);

        assertThrows(SecurityException.class,
                () -> service.handleRazorpayWebhook(body, signature(body)));
        verify(events).insertIfAbsent(any(), eq("RAZORPAY"), eq("evt_test_123"), eq("payment.captured"), anyString());
        verify(events, never()).save(any(BillingWebhookEvent.class));
    }

    @Test
    void rejectsSignedWebhookWhenGatewayAmountDoesNotMatchPayment() throws Exception {
        assertWebhookPaymentMismatchRejected("9999", "INR");
    }

    @Test
    void rejectsSignedWebhookWhenGatewayCurrencyDoesNotMatchPayment() throws Exception {
        assertWebhookPaymentMismatchRejected("10000", "USD");
    }

    @Test
    void rejectsDifferentPaymentForAnAlreadyPaidOrder() throws Exception {
        String body = "{\"id\":\"evt_second_payment\",\"event\":\"payment.captured\","
                + "\"payload\":{\"payment\":{\"entity\":{\"id\":\"pay_second_123\","
                + "\"order_id\":\"order_paid_123\",\"amount\":10000,\"currency\":\"INR\"}}}}";
        BillingWebhookEvent existing = mock(BillingWebhookEvent.class);
        when(existing.getPayloadHash()).thenReturn(hash(body));
        when(existing.getStatus()).thenReturn("RECEIVED");
        BillingWebhookEventRepository events = mock(BillingWebhookEventRepository.class);
        when(events.findByProviderAndEventId("RAZORPAY", "evt_second_payment"))
                .thenReturn(Optional.of(existing));

        var orderId = java.util.UUID.randomUUID();
        var order = new BillingOrder(orderId, java.util.UUID.randomUUID(), java.util.UUID.randomUUID(),
                "MONTHLY", new java.math.BigDecimal("100.00"), "INR");
        order.setExternalOrderId("order_paid_123");
        order.markPaid();
        var payment = new BillingPayment(java.util.UUID.randomUUID(), orderId, "RAZORPAY",
                new java.math.BigDecimal("100.00"), "INR");
        payment.capture("pay_original_123");
        BillingOrderRepository orders = mock(BillingOrderRepository.class);
        BillingPaymentRepository payments = mock(BillingPaymentRepository.class);
        when(orders.findByExternalOrderIdForUpdate("order_paid_123")).thenReturn(Optional.of(order));
        when(payments.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        BillingOrderService service = new BillingOrderService(
                orders, payments, mock(BillingInvoiceRepository.class), mock(BillingRefundRepository.class),
                mock(BillingRefundAttemptService.class), events, mock(SubscriptionPlanRepository.class),
                mock(UserSubscriptionService.class), new ObjectMapper(),
                "test-key-id", "test-key-secret", SECRET, "https://api.razorpay.com/v1");

        assertThrows(IllegalStateException.class,
                () -> service.handleRazorpayWebhook(body, signature(body)));
        verify(events, never()).save(any(BillingWebhookEvent.class));
        verify(payments, never()).save(any(BillingPayment.class));
    }

    private void assertWebhookPaymentMismatchRejected(String amount, String currency) throws Exception {
        String body = "{\"id\":\"evt_amount_check\",\"event\":\"payment.captured\","
                + "\"payload\":{\"payment\":{\"entity\":{\"id\":\"pay_test_123\","
                + "\"order_id\":\"order_test_123\",\"amount\":" + amount
                + ",\"currency\":\"" + currency + "\"}}}}";
        BillingWebhookEvent existing = mock(BillingWebhookEvent.class);
        when(existing.getPayloadHash()).thenReturn(hash(body));
        when(existing.getStatus()).thenReturn("RECEIVED");
        BillingWebhookEventRepository events = mock(BillingWebhookEventRepository.class);
        when(events.findByProviderAndEventId("RAZORPAY", "evt_amount_check"))
                .thenReturn(Optional.of(existing));

        var orderId = java.util.UUID.randomUUID();
        var order = new BillingOrder(orderId, java.util.UUID.randomUUID(), java.util.UUID.randomUUID(),
                "MONTHLY", new java.math.BigDecimal("100.00"), "INR");
        order.setExternalOrderId("order_test_123");
        var payment = new BillingPayment(java.util.UUID.randomUUID(), orderId, "RAZORPAY",
                new java.math.BigDecimal("100.00"), "INR");
        BillingOrderRepository orders = mock(BillingOrderRepository.class);
        BillingPaymentRepository payments = mock(BillingPaymentRepository.class);
        when(orders.findByExternalOrderIdForUpdate("order_test_123")).thenReturn(Optional.of(order));
        when(payments.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        BillingOrderService service = new BillingOrderService(
                orders, payments, mock(BillingInvoiceRepository.class), mock(BillingRefundRepository.class),
                mock(BillingRefundAttemptService.class), events, mock(SubscriptionPlanRepository.class),
                mock(UserSubscriptionService.class), new ObjectMapper(),
                "test-key-id", "test-key-secret", SECRET, "https://api.razorpay.com/v1");

        assertThrows(SecurityException.class, () -> service.handleRazorpayWebhook(body, signature(body)));
        verify(payments, never()).save(any(BillingPayment.class));
        verify(orders, never()).save(any(BillingOrder.class));
    }

    @Test
    void usesProviderEventHeaderWhenWebhookBodyHasNoEventId() throws Exception {
        String body = "{\"event\":\"payment.captured\","
                + "\"payload\":{\"payment\":{\"entity\":{\"id\":\"pay_test_123\","
                + "\"order_id\":\"order_test_123\"}}}}";
        BillingWebhookEvent existing = mock(BillingWebhookEvent.class);
        when(existing.getPayloadHash()).thenReturn(hash(body));
        when(existing.getStatus()).thenReturn("PROCESSED");
        BillingWebhookEventRepository events = mock(BillingWebhookEventRepository.class);
        when(events.findByProviderAndEventId("RAZORPAY", "evt_header_123"))
                .thenReturn(Optional.of(existing));
        BillingOrderService service = service(events);
        String signature = signature(body);

        assertDoesNotThrow(() -> service.handleRazorpayWebhook(body, signature, "evt_header_123"));
        verify(events).insertIfAbsent(any(), eq("RAZORPAY"), eq("evt_header_123"),
                eq("payment.captured"), anyString());
    }

    @Test
    void rejectsSupportedWebhookWithoutProviderEventIdInsteadOfUsingPaymentIdAsFallback() throws Exception {
        String body = "{\"event\":\"payment.captured\","
                + "\"payload\":{\"payment\":{\"entity\":{\"id\":\"pay_test_123\","
                + "\"order_id\":\"order_test_123\"}}}}";
        BillingWebhookEventRepository events = mock(BillingWebhookEventRepository.class);
        BillingOrderService service = service(events);

        assertThrows(IllegalArgumentException.class,
                () -> service.handleRazorpayWebhook(body, signature(body)));
        verifyNoInteractions(events);
    }

    @Test
    void rejectsInvalidSignatureBeforeReadingEventStore() {
        BillingWebhookEventRepository events = mock(BillingWebhookEventRepository.class);
        BillingOrderService service = service(events);

        assertThrows(SecurityException.class,
                () -> service.handleRazorpayWebhook(validPayload(), "invalid-signature"));
        verifyNoInteractions(events);
    }

    @Test
    void ignoresUnsupportedSignedEventsWithoutPersistingThem() throws Exception {
        String body = "{\"id\":\"evt_refund_123\",\"event\":\"refund.created\"}";
        BillingWebhookEventRepository events = mock(BillingWebhookEventRepository.class);
        BillingOrderService service = service(events);

        assertDoesNotThrow(() -> service.handleRazorpayWebhook(body, signature(body)));
        verifyNoInteractions(events);
    }

    @Test
    void rejectsRefundAbovePaidAmountBeforeCallingGateway() {
        var orderId = java.util.UUID.randomUUID();
        var order = new BillingOrder(orderId, java.util.UUID.randomUUID(), java.util.UUID.randomUUID(),
                "MONTHLY", new java.math.BigDecimal("100.00"), "INR");
        order.markPaid();
        var payment = new BillingPayment(java.util.UUID.randomUUID(), orderId, "RAZORPAY",
                new java.math.BigDecimal("100.00"), "INR");
        payment.capture("pay_test_123");

        BillingOrderRepository orders = mock(BillingOrderRepository.class);
        BillingPaymentRepository payments = mock(BillingPaymentRepository.class);
        BillingRefundRepository refunds = mock(BillingRefundRepository.class);
        when(orders.findById(orderId)).thenReturn(Optional.of(order));
        when(orders.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(payments.findByOrderId(orderId)).thenReturn(Optional.of(payment));
        when(refunds.findByPaymentIdAndStatus(payment.getId(), "REFUNDED")).thenReturn(java.util.List.of());

        BillingOrderService service = new BillingOrderService(
                orders, payments, mock(BillingInvoiceRepository.class), refunds,
                mock(BillingRefundAttemptService.class), mock(BillingWebhookEventRepository.class), mock(SubscriptionPlanRepository.class),
                mock(UserSubscriptionService.class), new ObjectMapper(),
                "test-key-id", "test-key-secret", SECRET, "https://api.razorpay.com/v1");

        assertThrows(IllegalArgumentException.class,
                () -> service.refund(orderId, new java.math.BigDecimal("100.01"), "test over-refund"));
        verify(orders).findByIdForUpdate(orderId);
        verify(refunds, never()).save(any(BillingRefund.class));
    }

    @Test
    void rejectsRefundWithSubCentPrecisionBeforeCallingGateway() {
        var orderId = java.util.UUID.randomUUID();
        var order = new BillingOrder(orderId, java.util.UUID.randomUUID(), java.util.UUID.randomUUID(),
                "MONTHLY", new java.math.BigDecimal("100.00"), "INR");
        order.markPaid();
        var payment = new BillingPayment(java.util.UUID.randomUUID(), orderId, "RAZORPAY",
                new java.math.BigDecimal("100.00"), "INR");
        payment.capture("pay_test_123");

        BillingOrderRepository orders = mock(BillingOrderRepository.class);
        BillingPaymentRepository payments = mock(BillingPaymentRepository.class);
        BillingRefundRepository refunds = mock(BillingRefundRepository.class);
        when(orders.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(payments.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        BillingOrderService service = new BillingOrderService(
                orders, payments, mock(BillingInvoiceRepository.class), refunds,
                mock(BillingRefundAttemptService.class), mock(BillingWebhookEventRepository.class), mock(SubscriptionPlanRepository.class),
                mock(UserSubscriptionService.class), new ObjectMapper(),
                "test-key-id", "test-key-secret", SECRET, "https://api.razorpay.com/v1");

        assertThrows(IllegalArgumentException.class,
                () -> service.refund(orderId, new java.math.BigDecimal("1.001"), "precision test"));
        verify(refunds, never()).save(any(BillingRefund.class));
        verify(refunds, never()).findByPaymentIdAndStatus(any(), anyString());
    }

    @Test
    void rejectsOverlongRefundReasonBeforeCallingGateway() {
        var orderId = java.util.UUID.randomUUID();
        var order = new BillingOrder(orderId, java.util.UUID.randomUUID(), java.util.UUID.randomUUID(),
                "MONTHLY", new java.math.BigDecimal("100.00"), "INR");
        order.markPaid();
        var payment = new BillingPayment(java.util.UUID.randomUUID(), orderId, "RAZORPAY",
                new java.math.BigDecimal("100.00"), "INR");
        payment.capture("pay_test_123");

        BillingOrderRepository orders = mock(BillingOrderRepository.class);
        BillingPaymentRepository payments = mock(BillingPaymentRepository.class);
        BillingRefundRepository refunds = mock(BillingRefundRepository.class);
        when(orders.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(payments.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        BillingOrderService service = new BillingOrderService(
                orders, payments, mock(BillingInvoiceRepository.class), refunds,
                mock(BillingRefundAttemptService.class), mock(BillingWebhookEventRepository.class), mock(SubscriptionPlanRepository.class),
                mock(UserSubscriptionService.class), new ObjectMapper(),
                "test-key-id", "test-key-secret", SECRET, "https://api.razorpay.com/v1");

        assertThrows(IllegalArgumentException.class,
                () -> service.refund(orderId, new java.math.BigDecimal("1.00"), "r".repeat(501)));
        verify(refunds, never()).save(any(BillingRefund.class));
        verify(refunds, never()).findByPaymentIdAndStatus(any(), anyString());
    }

    @Test
    void unresolvedRefundAttemptReservesAmountAndPreventsDuplicateRefund() {
        var orderId = java.util.UUID.randomUUID();
        var order = new BillingOrder(orderId, java.util.UUID.randomUUID(), java.util.UUID.randomUUID(),
                "MONTHLY", new java.math.BigDecimal("100.00"), "INR");
        order.markPaid();
        var paymentId = java.util.UUID.randomUUID();
        var payment = new BillingPayment(paymentId, orderId, "RAZORPAY",
                new java.math.BigDecimal("100.00"), "INR");
        payment.capture("pay_test_123");
        var unresolved = new BillingRefund(java.util.UUID.randomUUID(), paymentId, orderId,
                order.getUserId(), "RAZORPAY", new java.math.BigDecimal("100.00"), "INR", "previous attempt");

        BillingOrderRepository orders = mock(BillingOrderRepository.class);
        BillingPaymentRepository payments = mock(BillingPaymentRepository.class);
        BillingRefundRepository refunds = mock(BillingRefundRepository.class);
        when(orders.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(payments.findByOrderId(orderId)).thenReturn(Optional.of(payment));
        when(refunds.findByPaymentIdAndStatusIn(eq(paymentId), anyCollection()))
                .thenReturn(java.util.List.of(unresolved));

        BillingOrderService service = new BillingOrderService(
                orders, payments, mock(BillingInvoiceRepository.class), refunds,
                mock(BillingRefundAttemptService.class), mock(BillingWebhookEventRepository.class),
                mock(SubscriptionPlanRepository.class), mock(UserSubscriptionService.class),
                new ObjectMapper(), "test-key-id", "test-key-secret", SECRET,
                "https://api.razorpay.com/v1");

        assertThrows(IllegalArgumentException.class,
                () -> service.refund(orderId, new java.math.BigDecimal("1.00"), "duplicate attempt"));
        verify(refunds).findByPaymentIdAndStatusIn(eq(paymentId), anyCollection());
        verify(refunds, never()).save(any(BillingRefund.class));
    }

    @Test
    void paymentVerificationRejectsAnotherUsersOrderBeforePaymentLookup() {
        var ownerId = java.util.UUID.randomUUID();
        var otherUserId = java.util.UUID.randomUUID();
        var orderId = java.util.UUID.randomUUID();
        var order = new BillingOrder(orderId, ownerId, java.util.UUID.randomUUID(),
                "MONTHLY", new java.math.BigDecimal("100.00"), "INR");
        order.setExternalOrderId("order_gateway_123");
        BillingOrderRepository orders = mock(BillingOrderRepository.class);
        BillingPaymentRepository payments = mock(BillingPaymentRepository.class);
        when(orders.findById(orderId)).thenReturn(Optional.of(order));
        when(orders.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        BillingOrderService service = new BillingOrderService(
                orders, payments, mock(BillingInvoiceRepository.class), mock(BillingRefundRepository.class),
                mock(BillingRefundAttemptService.class), mock(BillingWebhookEventRepository.class), mock(SubscriptionPlanRepository.class),
                mock(UserSubscriptionService.class), new ObjectMapper(),
                "test-key-id", "test-key-secret", SECRET, "https://api.razorpay.com/v1");

        assertThrows(SecurityException.class,
                () -> service.verifyPayment(otherUserId, orderId.toString(),
                        "order_gateway_123", "pay_123", "signature"));
        verifyNoInteractions(payments);
    }

    @Test
    void paidOrderVerificationRejectsDifferentPaymentId() throws Exception {
        var ownerId = java.util.UUID.randomUUID();
        var orderId = java.util.UUID.randomUUID();
        var order = new BillingOrder(orderId, ownerId, java.util.UUID.randomUUID(),
                "MONTHLY", new java.math.BigDecimal("100.00"), "INR");
        order.setExternalOrderId("order_gateway_123");
        order.markPaid();
        var payment = new BillingPayment(java.util.UUID.randomUUID(), orderId, "RAZORPAY",
                new java.math.BigDecimal("100.00"), "INR");
        payment.capture("pay_original_123");
        BillingOrderRepository orders = mock(BillingOrderRepository.class);
        BillingPaymentRepository payments = mock(BillingPaymentRepository.class);
        when(orders.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(payments.findByOrderId(orderId)).thenReturn(Optional.of(payment));
        BillingOrderService service = new BillingOrderService(
                orders, payments, mock(BillingInvoiceRepository.class), mock(BillingRefundRepository.class),
                mock(BillingRefundAttemptService.class), mock(BillingWebhookEventRepository.class),
                mock(SubscriptionPlanRepository.class), mock(UserSubscriptionService.class),
                new ObjectMapper(), "test-key-id", "test-key-secret", SECRET,
                "https://api.razorpay.com/v1");

        String paymentId = "pay_attacker_456";
        String signature = hmac("order_gateway_123|" + paymentId, "test-key-secret");
        assertThrows(SecurityException.class, () -> service.verifyPayment(ownerId, orderId.toString(),
                "order_gateway_123", paymentId, signature));
        verify(payments, never()).save(any(BillingPayment.class));
        verify(orders, never()).save(any(BillingOrder.class));
    }

    @Test
    void validPaymentVerificationCapturesPaymentAndActivatesSubscription() throws Exception {
        var ownerId = java.util.UUID.randomUUID();
        var orderId = java.util.UUID.randomUUID();
        var planId = java.util.UUID.randomUUID();
        var order = new BillingOrder(orderId, ownerId, planId,
                "MONTHLY", new java.math.BigDecimal("100.00"), "INR");
        order.setExternalOrderId("order_gateway_123");
        var payment = new BillingPayment(java.util.UUID.randomUUID(), orderId, "RAZORPAY",
                new java.math.BigDecimal("100.00"), "INR");
        var plan = mock(com.masterlearning.platform.subscription.SubscriptionPlan.class);
        when(plan.getCode()).thenReturn("PRO");
        BillingOrderRepository orders = mock(BillingOrderRepository.class);
        BillingPaymentRepository payments = mock(BillingPaymentRepository.class);
        BillingInvoiceRepository invoices = mock(BillingInvoiceRepository.class);
        SubscriptionPlanRepository plans = mock(SubscriptionPlanRepository.class);
        UserSubscriptionService subscriptions = mock(UserSubscriptionService.class);
        when(orders.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(payments.findByOrderId(orderId)).thenReturn(Optional.of(payment));
        when(invoices.existsByOrderId(orderId)).thenReturn(false);
        when(plans.findById(planId)).thenReturn(Optional.of(plan));
        BillingOrderService service = new BillingOrderService(
                orders, payments, invoices, mock(BillingRefundRepository.class),
                mock(BillingRefundAttemptService.class), mock(BillingWebhookEventRepository.class),
                plans, subscriptions, new ObjectMapper(), "test-key-id", "test-key-secret", SECRET,
                "https://api.razorpay.com/v1");

        String signature = hmac("order_gateway_123|pay_valid_123", "test-key-secret");
        service.verifyPayment(ownerId, orderId.toString(), "order_gateway_123", "pay_valid_123", signature);

        org.junit.jupiter.api.Assertions.assertEquals("PAID", order.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals("CAPTURED", payment.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals("pay_valid_123", payment.getProviderPaymentId());
        verify(payments).save(payment);
        verify(orders).save(order);
        verify(invoices).save(any(BillingInvoice.class));
        verify(subscriptions).activatePending(eq(ownerId), eq("PRO"), eq("MONTHLY"),
                any(java.time.LocalDate.class), any(java.time.LocalDate.class), eq("pay_valid_123"));
    }

    @Test
    void paidOrderVerificationIsIdempotentForTheOriginalPayment() throws Exception {
        var ownerId = java.util.UUID.randomUUID();
        var orderId = java.util.UUID.randomUUID();
        var order = new BillingOrder(orderId, ownerId, java.util.UUID.randomUUID(),
                "MONTHLY", new java.math.BigDecimal("100.00"), "INR");
        order.setExternalOrderId("order_gateway_123");
        order.markPaid();
        var payment = new BillingPayment(java.util.UUID.randomUUID(), orderId, "RAZORPAY",
                new java.math.BigDecimal("100.00"), "INR");
        payment.capture("pay_original_123");
        BillingOrderRepository orders = mock(BillingOrderRepository.class);
        BillingPaymentRepository payments = mock(BillingPaymentRepository.class);
        when(orders.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(payments.findByOrderId(orderId)).thenReturn(Optional.of(payment));
        BillingOrderService service = new BillingOrderService(
                orders, payments, mock(BillingInvoiceRepository.class), mock(BillingRefundRepository.class),
                mock(BillingRefundAttemptService.class), mock(BillingWebhookEventRepository.class),
                mock(SubscriptionPlanRepository.class), mock(UserSubscriptionService.class),
                new ObjectMapper(), "test-key-id", "test-key-secret", SECRET,
                "https://api.razorpay.com/v1");

        String signature = hmac("order_gateway_123|pay_original_123", "test-key-secret");
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.verifyPayment(ownerId, orderId.toString(),
                "order_gateway_123", "pay_original_123", signature));
        verify(payments, never()).save(any(BillingPayment.class));
        verify(orders, never()).save(any(BillingOrder.class));
    }

    @Test
    void paymentVerificationRejectsInvalidSignatureWithoutCapturingPayment() {
        var ownerId = java.util.UUID.randomUUID();
        var orderId = java.util.UUID.randomUUID();
        var order = new BillingOrder(orderId, ownerId, java.util.UUID.randomUUID(),
                "MONTHLY", new java.math.BigDecimal("100.00"), "INR");
        order.setExternalOrderId("order_gateway_123");
        var payment = new BillingPayment(java.util.UUID.randomUUID(), orderId, "RAZORPAY",
                new java.math.BigDecimal("100.00"), "INR");
        BillingOrderRepository orders = mock(BillingOrderRepository.class);
        BillingPaymentRepository payments = mock(BillingPaymentRepository.class);
        when(orders.findById(orderId)).thenReturn(Optional.of(order));
        when(orders.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(payments.findByOrderId(orderId)).thenReturn(Optional.of(payment));
        BillingOrderService service = new BillingOrderService(
                orders, payments, mock(BillingInvoiceRepository.class), mock(BillingRefundRepository.class),
                mock(BillingRefundAttemptService.class), mock(BillingWebhookEventRepository.class), mock(SubscriptionPlanRepository.class),
                mock(UserSubscriptionService.class), new ObjectMapper(),
                "test-key-id", "test-key-secret", SECRET, "https://api.razorpay.com/v1");

        assertThrows(SecurityException.class,
                () -> service.verifyPayment(ownerId, orderId.toString(),
                        "order_gateway_123", "pay_123", "invalid-signature"));
        verify(payments, never()).save(any(BillingPayment.class));
        org.junit.jupiter.api.Assertions.assertEquals("CREATED", payment.getStatus());
    }

    private BillingOrderService service(BillingWebhookEventRepository events) {
        return new BillingOrderService(
                mock(BillingOrderRepository.class),
                mock(BillingPaymentRepository.class),
                mock(BillingInvoiceRepository.class),
                mock(BillingRefundRepository.class),
                mock(BillingRefundAttemptService.class), events,
                mock(SubscriptionPlanRepository.class),
                mock(UserSubscriptionService.class),
                new ObjectMapper(),
                "test-key-id",
                "test-key-secret",
                SECRET,
                "https://api.razorpay.com/v1");
    }

    private String validPayload() {
        return "{\"id\":\"evt_test_123\",\"event\":\"payment.captured\","
                + "\"payload\":{\"payment\":{\"entity\":{\"id\":\"pay_test_123\","
                + "\"order_id\":\"order_test_123\"}}}}";
    }

    private String hmac(String payload, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }

    private String signature(String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }

    private String hash(String body) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(body.getBytes(StandardCharsets.UTF_8)));
    }

    private void verifyNoInteractionsExceptLookup(BillingWebhookEventRepository events) {
        verify(events).insertIfAbsent(any(), eq("RAZORPAY"), eq("evt_test_123"), eq("payment.captured"), anyString());
        verify(events).findByProviderAndEventId("RAZORPAY", "evt_test_123");
        verify(events, never()).save(any(BillingWebhookEvent.class));
    }
}
