package com.masterlearning.platform.subscription;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SubscriptionPlanService {
    private final SubscriptionPlanRepository plans;
    private final SubscriptionPlanFeatureRepository features;

    public SubscriptionPlanService(SubscriptionPlanRepository plans, SubscriptionPlanFeatureRepository features) {
        this.plans = plans;
        this.features = features;
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> activePlans() {
        var active = plans.findActivePlans();
        var ids = active.stream().map(SubscriptionPlan::getId).toList();
        var grouped = features.findByPlanIdIn(ids).stream()
            .collect(Collectors.groupingBy(SubscriptionPlanFeature::getPlanId, LinkedHashMap::new, Collectors.toMap(
                SubscriptionPlanFeature::getFeatureCode,
                f -> f.getFeatureValue() == null ? "" : f.getFeatureValue(),
                (a,b) -> b,
                LinkedHashMap::new
            )));
        return active.stream().map(plan -> new SubscriptionPlanResponse(
            plan.getCode(), plan.getName(), plan.getDescription(),
            plan.getMonthlyPrice(), plan.getYearlyPrice(), plan.getCurrency(),
            grouped.getOrDefault(plan.getId(), Map.of())
        )).toList();
    }

    @Transactional
    public SubscriptionPlanResponse updatePlan(String code, SubscriptionPlanUpdateRequest request) {
        var plan = plans.findByCode(code.toUpperCase())
            .orElseThrow(() -> new IllegalArgumentException("Subscription plan not found: " + code));
        plan.update(request.name(), request.description(), request.monthlyPrice(), request.yearlyPrice(), request.currency(), request.active());
        plans.save(plan);

        if (request.features() != null) {
            features.deleteAll(features.findByPlanId(plan.getId()));
            request.features().forEach((featureCode, featureValue) -> features.save(new SubscriptionPlanFeature(UUID.randomUUID(), plan.getId(), featureCode, featureValue)));

        }
        return activePlans().stream()
            .filter(item -> item.code().equalsIgnoreCase(plan.getCode()))
            .findFirst()
            .orElseThrow();
    }
}
