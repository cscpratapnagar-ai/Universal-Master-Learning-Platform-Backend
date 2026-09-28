package com.masterlearning.platform.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface BillingRefundRepository extends JpaRepository<BillingRefund, UUID> {
    java.util.List<BillingRefund> findByPaymentIdAndStatus(UUID paymentId, String status);
    Optional<BillingRefund> findByProviderAndProviderRefundId(String provider, String providerRefundId);
}