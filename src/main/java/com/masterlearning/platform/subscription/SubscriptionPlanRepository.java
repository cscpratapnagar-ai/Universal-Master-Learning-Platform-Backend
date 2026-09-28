package com.masterlearning.platform.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {
    @Query("select p from SubscriptionPlan p where p.active = true order by p.monthlyPrice asc")
    List<SubscriptionPlan> findActivePlans();

    java.util.Optional<SubscriptionPlan> findByCode(String code);
}
