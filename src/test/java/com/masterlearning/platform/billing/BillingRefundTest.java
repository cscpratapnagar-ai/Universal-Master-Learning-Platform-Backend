package com.masterlearning.platform.billing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BillingRefundTest {
    @Test
    void ambiguousGatewayOutcomeCanBePersistedWithoutCallingItFailed() {
        BillingRefund refund = new BillingRefund(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "RAZORPAY", new BigDecimal("25.00"), "INR", "gateway timeout");

        refund.reconciliationRequired();

        assertEquals("RECONCILIATION_REQUIRED", refund.getStatus());
        assertEquals(new BigDecimal("25.00"), refund.getAmount());
        assertNull(refund.getProviderRefundId());
    }

    @Test
    void reconciliationCanBeResolvedByConfirmedProviderRefund() {
        BillingRefund refund = new BillingRefund(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "RAZORPAY", new BigDecimal("25.00"), "INR", "gateway timeout");

        refund.reconciliationRequired();
        refund.refunded("rfnd_reconciled_123");

        assertEquals("REFUNDED", refund.getStatus());
        assertEquals("rfnd_reconciled_123", refund.getProviderRefundId());
    }

    @Test
    void providerRefundIdMustBePresentAndNonBlank() {
        BillingRefund refund = new BillingRefund(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "RAZORPAY", new BigDecimal("25.00"), "INR", "confirmed");

        assertThrows(IllegalArgumentException.class, () -> refund.refunded(null));
        assertThrows(IllegalArgumentException.class, () -> refund.refunded("  "));
        assertEquals("REQUESTED", refund.getStatus());
        assertNull(refund.getProviderRefundId());
    }

    @Test
    void repeatedConfirmationIsIdempotentButDifferentProviderIdIsRejected() {
        BillingRefund refund = new BillingRefund(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "RAZORPAY", new BigDecimal("25.00"), "INR", "confirmed");

        refund.refunded("rfnd_test_123");
        refund.refunded("rfnd_test_123");

        assertEquals("REFUNDED", refund.getStatus());
        assertEquals("rfnd_test_123", refund.getProviderRefundId());
        assertThrows(IllegalStateException.class, () -> refund.refunded("rfnd_different_456"));
        assertThrows(IllegalStateException.class, refund::failed);
        assertThrows(IllegalStateException.class, refund::reconciliationRequired);
    }

    @Test
    void confirmedRefundStoresProviderIdAndFinalStatus() {
        BillingRefund refund = new BillingRefund(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "RAZORPAY", new BigDecimal("25.00"), "INR", "confirmed");

        refund.refunded("rfnd_test_123");

        assertEquals("REFUNDED", refund.getStatus());
        assertEquals("rfnd_test_123", refund.getProviderRefundId());
    }
}
