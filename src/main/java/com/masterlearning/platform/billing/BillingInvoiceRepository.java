package com.masterlearning.platform.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BillingInvoiceRepository extends JpaRepository<BillingInvoice, UUID> {
    List<BillingInvoice> findTop50ByUserIdOrderByIssuedAtDesc(UUID userId);
    boolean existsByOrderId(UUID orderId);
}