package com.masterlearning.platform.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SubscriptionPlanFeatureRepository extends JpaRepository<SubscriptionPlanFeature, UUID> {
    List<SubscriptionPlanFeature> findByPlanIdIn(List<UUID> planIds);
    List<SubscriptionPlanFeature> findByPlanId(UUID planId);
}
