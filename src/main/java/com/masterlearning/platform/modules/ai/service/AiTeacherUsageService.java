package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.AiTeacherQuotaResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class AiTeacherUsageService {

    private final JdbcTemplate jdbc;
    private final AiTeacherEntitlementService entitlements;

    public AiTeacherUsageService(
            JdbcTemplate jdbc,
            AiTeacherEntitlementService entitlements) {
        this.jdbc = jdbc;
        this.entitlements = entitlements;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AiTeacherQuotaResponse consumeTurn(UUID userId) {
        var entitlement = entitlements.getEntitlement(userId);
        LocalDate periodStart = LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1);

        if (entitlement.monthlyLimit() < 0) {
            ensureUsageRow(userId, periodStart);
            return getQuota(userId, entitlement.plan(), entitlement.monthlyLimit(), periodStart);
        }

        String sql = """
                INSERT INTO ai_teacher_usage (
                    id, user_id, period_start, turn_count, created_at, updated_at
                )
                VALUES (?, ?, ?, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                ON CONFLICT (user_id, period_start)
                DO UPDATE SET
                    turn_count = ai_teacher_usage.turn_count + 1,
                    updated_at = CURRENT_TIMESTAMP
                WHERE ai_teacher_usage.turn_count < ?
                RETURNING turn_count
                """;

        List<Integer> rows = jdbc.query(
                sql,
                ps -> {
                    ps.setObject(1, UUID.randomUUID());
                    ps.setObject(2, userId);
                    ps.setObject(3, periodStart);
                    ps.setInt(4, entitlement.monthlyLimit());
                },
                (rs, rowNum) -> rs.getInt(1));

        if (rows.isEmpty()) {
            throw new AiTeacherQuotaExceededException(
                    entitlement.plan(),
                    entitlement.monthlyLimit(),
                    periodStart);
        }

        return getQuota(userId, entitlement.plan(), entitlement.monthlyLimit(), periodStart);
    }

    @Transactional(readOnly = true)
    public AiTeacherQuotaResponse getQuota(UUID userId) {
        var entitlement = entitlements.getEntitlement(userId);
        LocalDate periodStart = LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1);
        return getQuota(userId, entitlement.plan(), entitlement.monthlyLimit(), periodStart);
    }

    private AiTeacherQuotaResponse getQuota(
            UUID userId,
            String plan,
            int limit,
            LocalDate periodStart) {

        Integer used = jdbc.query(
                """
                SELECT turn_count
                FROM ai_teacher_usage
                WHERE user_id = ? AND period_start = ?
                """,
                ps -> {
                    ps.setObject(1, userId);
                    ps.setObject(2, periodStart);
                },
                (rs, rowNum) -> rs.getInt(1))
                .stream()
                .findFirst()
                .orElse(0);

        if (limit < 0) {
            return AiTeacherQuotaResponse.unlimited(plan, used, periodStart);
        }

        return new AiTeacherQuotaResponse(
                plan,
                used,
                limit,
                Math.max(0, limit - used),
                periodStart);
    }

    private void ensureUsageRow(UUID userId, LocalDate periodStart) {
        jdbc.update(
                """
                INSERT INTO ai_teacher_usage (
                    id, user_id, period_start, turn_count, created_at, updated_at
                )
                VALUES (?, ?, ?, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                ON CONFLICT (user_id, period_start) DO NOTHING
                """,
                UUID.randomUUID(),
                userId,
                periodStart);
    }

    public static class AiTeacherQuotaExceededException extends RuntimeException {
        private final String plan;
        private final int limit;
        private final LocalDate periodStart;

        public AiTeacherQuotaExceededException(String plan, int limit, LocalDate periodStart) {
            super("AI Teacher monthly quota reached");
            this.plan = plan;
            this.limit = limit;
            this.periodStart = periodStart;
        }

        public String getPlan() { return plan; }
        public int getLimit() { return limit; }
        public LocalDate getPeriodStart() { return periodStart; }
    }
}
