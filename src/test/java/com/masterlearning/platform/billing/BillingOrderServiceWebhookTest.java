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
        verify(events, never()).save(any(BillingWebhookEvent.class));
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
                mock(BillingWebhookEventRepository.class), mock(SubscriptionPlanRepository.class),
                mock(UserSubscriptionService.class), new ObjectMapper(),
                "test-key-id", "test-key-secret", SECRET, "https://api.razorpay.com/v1");

        assertThrows(IllegalArgumentException.class,
                () -> service.refund(orderId, new java.math.BigDecimal("100.01"), "test over-refund"));
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
                mock(BillingWebhookEventRepository.class), mock(SubscriptionPlanRepository.class),
                mock(UserSubscriptionService.class), new ObjectMapper(),
                "test-key-id", "test-key-secret", SECRET, "https://api.razorpay.com/v1");

        assertThrows(SecurityException.class,
                () -> service.verifyPayment(otherUserId, orderId.toString(),
                        "order_gateway_123", "pay_123", "signature"));
        verifyNoInteractions(payments);
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
        when(payments.findByOrderId(orderId)).thenReturn(Optional.of(payment));
        BillingOrderService service = new BillingOrderService(
                orders, payments, mock(BillingInvoiceRepository.class), mock(BillingRefundRepository.class),
                mock(BillingWebhookEventRepository.class), mock(SubscriptionPlanRepository.class),
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
                events,
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
        verify(events).findByProviderAndEventId("RAZORPAY", "evt_test_123");
        verify(events, never()).save(any(BillingWebhookEvent.class));
    }
}
