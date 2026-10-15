package com.masterlearning.platform.billing;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillingOrderRepository extends JpaRepository<BillingOrder, UUID> {
    List<BillingOrder> findTop20ByUserIdOrderByCreatedAtDesc(UUID userId);
    List<BillingOrder> findTop100ByOrderByCreatedAtDesc();
    Optional<BillingOrder> findByExternalOrderId(String externalOrderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from BillingOrder o where o.id = :id")
    Optional<BillingOrder> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from BillingOrder o where o.externalOrderId = :externalOrderId")
    Optional<BillingOrder> findByExternalOrderIdForUpdate(@Param("externalOrderId") String externalOrderId);
}
