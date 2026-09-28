package com.masterlearning.platform.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BillingOrderRepository extends JpaRepository<BillingOrder, UUID> {
    List<BillingOrder> findTop20ByUserIdOrderByCreatedAtDesc(UUID userId);
    List<BillingOrder> findTop100ByOrderByCreatedAtDesc();
    java.util.Optional<BillingOrder> findByExternalOrderId(String externalOrderId);
}