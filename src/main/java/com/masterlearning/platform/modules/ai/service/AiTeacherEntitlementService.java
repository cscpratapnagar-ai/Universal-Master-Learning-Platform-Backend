package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.user.entity.User;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.subscription.SubscriptionPlan;
import com.masterlearning.platform.subscription.SubscriptionPlanFeature;
import com.masterlearning.platform.subscription.SubscriptionPlanFeatureRepository;
import com.masterlearning.platform.subscription.SubscriptionPlanRepository;
import com.masterlearning.platform.subscription.UserSubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AiTeacherEntitlementService {

    private static final String AI_TEACHER_MONTHLY_TURNS = "AI_TEACHER_MONTHLY_TURNS";

    private final UserRepository users;
    private final SubscriptionPlanRepository plans;
    private final SubscriptionPlanFeatureRepository features;
    private final UserSubscriptionRepository subscriptions;

    public AiTeacherEntitlementService(
            UserRepository users,
            SubscriptionPlanRepository plans,
            SubscriptionPlanFeatureRepository features,
            UserSubscriptionRepository subscriptions) {
        this.users = users;
        this.plans = plans;
        this.features = features;
        this.subscriptions = subscriptions;
    }

    public Entitlement getEntitlement(UUID userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));

        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getCode() == null ? "" : role.getCode().toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());

        String planCode = subscriptions.findCurrentByUserId(userId)
                .filter(subscription -> "ACTIVE".equalsIgnoreCase(subscription.getStatus()))
                .filter(subscription -> !subscription.getCurrentPeriodEnd().isBefore(java.time.LocalDate.now()))
                .flatMap(subscription -> plans.findById(subscription.getPlanId()))
                .map(SubscriptionPlan::getCode)
                .orElseGet(() -> resolvePlanCode(roles));
        SubscriptionPlan plan = plans.findByCode(planCode)
                .orElseGet(() -> plans.findByCode("FREE")
                        .orElseThrow(() -> new IllegalStateException("FREE subscription plan is not configured")));

        int monthlyLimit = readMonthlyTurns(plan);
        return new Entitlement(plan.getCode(), monthlyLimit);
    }

    private String resolvePlanCode(Set<String> roles) {
        if (containsAny(roles, "SUPER_ADMIN", "ENTERPRISE", "AI_TEACHER_ENTERPRISE")) {
            return "ENTERPRISE";
        }
        if (containsAny(roles, "PRO", "AI_TEACHER_PRO")) {
            return "PRO";
        }
        if (containsAny(roles, "PREMIUM", "AI_TEACHER_PREMIUM")) {
            return "PREMIUM";
        }
        return "FREE";
    }

    private int readMonthlyTurns(SubscriptionPlan plan) {
        return features.findByPlanId(plan.getId()).stream()
                .filter(feature -> AI_TEACHER_MONTHLY_TURNS.equalsIgnoreCase(feature.getFeatureCode()))
                .map(SubscriptionPlanFeature::getFeatureValue)
                .findFirst()
                .map(this::parseLimit)
                .orElse(0);
    }

    private int parseLimit(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        if ("UNLIMITED".equalsIgnoreCase(value.trim())) {
            return -1;
        }
        try {
            return Math.max(0, Integer.parseInt(value.trim()));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private boolean containsAny(Set<String> roles, String... expected) {
        for (String role : expected) {
            if (roles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    public record Entitlement(String plan, int monthlyLimit) {}
}
