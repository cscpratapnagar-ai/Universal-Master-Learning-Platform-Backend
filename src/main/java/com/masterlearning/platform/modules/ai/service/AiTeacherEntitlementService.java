package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.user.entity.User;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class AiTeacherEntitlementService {

    private final UserRepository users;
    private final int freeMonthlyTurns;
    private final int premiumMonthlyTurns;
    private final int proMonthlyTurns;

    public AiTeacherEntitlementService(
            UserRepository users,
            @Value("${app.ai.teacher.quota.free-monthly-turns:25}") int freeMonthlyTurns,
            @Value("${app.ai.teacher.quota.premium-monthly-turns:500}") int premiumMonthlyTurns,
            @Value("${app.ai.teacher.quota.pro-monthly-turns:2000}") int proMonthlyTurns) {
        this.users = users;
        this.freeMonthlyTurns = freeMonthlyTurns;
        this.premiumMonthlyTurns = premiumMonthlyTurns;
        this.proMonthlyTurns = proMonthlyTurns;
    }

    public Entitlement getEntitlement(UUID userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));

        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getCode() == null ? "" : role.getCode().toUpperCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());

        if (containsAny(roles, "SUPER_ADMIN", "ENTERPRISE", "AI_TEACHER_ENTERPRISE")) {
            return new Entitlement("ENTERPRISE", -1);
        }
        if (containsAny(roles, "PRO", "AI_TEACHER_PRO")) {
            return new Entitlement("PRO", proMonthlyTurns);
        }
        if (containsAny(roles, "PREMIUM", "AI_TEACHER_PREMIUM")) {
            return new Entitlement("PREMIUM", premiumMonthlyTurns);
        }
        return new Entitlement("FREE", freeMonthlyTurns);
    }

    private boolean containsAny(Set<String> roles, String... expected) {
        for (String role : expected) {
            if (roles.contains(role)) return true;
        }
        return false;
    }

    public record Entitlement(String plan, int monthlyLimit) {}
}
