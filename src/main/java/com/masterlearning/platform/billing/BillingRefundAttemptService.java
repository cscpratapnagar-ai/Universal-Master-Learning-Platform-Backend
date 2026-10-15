package com.masterlearning.platform.billing;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Persists refund attempts independently from the transaction that calls the
 * payment gateway. A gateway timeout must not erase the local attempt record.
 */
@Service
public class BillingRefundAttemptService {
    private final BillingRefundRepository refunds;

    public BillingRefundAttemptService(BillingRefundRepository refunds) {
        this.refunds = refunds;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BillingRefund recordAttempt(BillingRefund refund) {
        return refunds.saveAndFlush(refund);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BillingRefund markFailed(UUID refundId) {
        BillingRefund refund = refunds.findById(refundId)
                .orElseThrow(() -> new IllegalStateException("Refund attempt record not found"));
        refund.failed();
        return refunds.saveAndFlush(refund);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BillingRefund markRefunded(UUID refundId, String providerRefundId) {
        BillingRefund refund = refunds.findById(refundId)
                .orElseThrow(() -> new IllegalStateException("Refund attempt record not found"));
        refund.refunded(providerRefundId);
        return refunds.saveAndFlush(refund);
    }
}