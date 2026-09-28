package com.masterlearning.platform.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface BillingPaymentRepository extends JpaRepository<BillingPayment, UUID> {
    Optional<BillingPayment> findByProviderAndProviderPaymentId(String provider, String providerPaymentId);
}
