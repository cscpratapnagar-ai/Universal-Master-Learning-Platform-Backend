package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.AiTeacherGovernanceOverviewResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class AiTeacherGovernanceService {

    private final JdbcTemplate jdbc;

    public AiTeacherGovernanceService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public AiTeacherGovernanceOverviewResponse getOverview() {
        LocalDate periodStart = LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1);

        Long activeUsers = jdbc.queryForObject(
                """
                SELECT COUNT(DISTINCT user_id)
                FROM ai_teacher_usage
                WHERE period_start = ?
                  AND turn_count > 0
                """,
                Long.class,
                periodStart);

        Long monthlyTurns = jdbc.queryForObject(
                """
                SELECT COALESCE(SUM(turn_count), 0)
                FROM ai_teacher_usage
                WHERE period_start = ?
                """,
                Long.class,
                periodStart);

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
