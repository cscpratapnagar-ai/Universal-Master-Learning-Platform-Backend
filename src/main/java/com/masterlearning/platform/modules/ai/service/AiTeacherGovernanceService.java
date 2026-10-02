package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.AiTeacherGovernanceOverviewResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class AiTeacherGovernanceService {

    private final JdbcTemplate jdbc;

    public AiTeacherGovernanceService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public AiTeacherGovernanceOverviewResponse getOverview(UUID currentUserId, boolean superAdmin) {
        LocalDate periodStart = LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1);

        Long activeUsers = jdbc.queryForObject(
                """
                SELECT COUNT(DISTINCT user_id)
                FROM ai_teacher_usage
                WHERE period_start = ?
                  AND turn_count > 0
                  AND (? = TRUE OR EXISTS (
                      SELECT 1 FROM organization_members om
                      WHERE om.user_id = ai_teacher_usage.user_id
                        AND om.active = TRUE
                        AND EXISTS (
                            SELECT 1 FROM organization_members scope
                            WHERE scope.user_id = ?
                              AND scope.active = TRUE
                              AND scope.organization_id = om.organization_id
                        )
                  ))
                """,
                Long.class,
                periodStart, superAdmin, currentUserId);

        Long monthlyTurns = jdbc.queryForObject(
                """
                SELECT COALESCE(SUM(turn_count), 0)
                FROM ai_teacher_usage
                WHERE period_start = ?
                  AND (? = TRUE OR EXISTS (
                      SELECT 1 FROM organization_members om
                      WHERE om.user_id = ai_teacher_usage.user_id
                        AND om.active = TRUE
                        AND EXISTS (
                            SELECT 1 FROM organization_members scope
                            WHERE scope.user_id = ?
                              AND scope.active = TRUE
                              AND scope.organization_id = om.organization_id
                        )
                  ))
                """,
                Long.class,
                periodStart, superAdmin, currentUserId);

        long users = activeUsers == null ? 0 : activeUsers;
        long turns = monthlyTurns == null ? 0 : monthlyTurns;
        long average = users == 0 ? 0 : turns / users;

        return new AiTeacherGovernanceOverviewResponse(
                users,
                turns,
                average,
                periodStart.toString());
    }
}
