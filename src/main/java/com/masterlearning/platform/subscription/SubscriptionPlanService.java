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
            request.features().forEach((featureCode, featureValue) -> {
                var feature = new SubscriptionPlanFeature();
                try {
                    var idField = SubscriptionPlanFeature.class.getDeclaredField("id");
                    var planField = SubscriptionPlanFeature.class.getDeclaredField("planId");
                    var codeField = SubscriptionPlanFeature.class.getDeclaredField("featureCode");
                    var valueField = SubscriptionPlanFeature.class.getDeclaredField("featureValue");
                    idField.setAccessible(true); planField.setAccessible(true); codeField.setAccessible(true); valueField.setAccessible(true);
                    idField.set(feature, UUID.randomUUID());
                    planField.set(feature, plan.getId());
                    codeField.set(feature, featureCode);
                    valueField.set(feature, featureValue);
                } catch (ReflectiveOperationException ex) {
                    throw new IllegalStateException("Unable to prepare plan feature", ex);
                }
                features.save(feature);
            });
        }
        return activePlans().stream()
            .filter(item -> item.code().equalsIgnoreCase(plan.getCode()))
            .findFirst()
            .orElseThrow();
    }
}
