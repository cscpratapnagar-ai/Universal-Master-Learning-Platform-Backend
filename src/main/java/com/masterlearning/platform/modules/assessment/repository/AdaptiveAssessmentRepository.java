package com.masterlearning.platform.modules.assessment.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class AdaptiveAssessmentRepository {
    private final JdbcTemplate jdbcTemplate;

    public AdaptiveAssessmentRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public List<QuestionSignal> findQuestionSignals(UUID assessmentId, UUID userId) {
        return jdbcTemplate.query("""
                SELECT q.id,
                       COALESCE(AVG(CASE WHEN at.id IS NOT NULL AND aa.correct THEN 1.0
                                         WHEN at.id IS NOT NULL THEN 0.0 END), 0.0) AS success_rate,
                       COUNT(at.id) AS learner_attempts,
                       COALESCE(MAX(CASE WHEN at.id IS NOT NULL AND aa.correct THEN 1 ELSE 0 END), 0) AS ever_correct,
                       MAX(at.submitted_at) AS last_answered_at,
                       COALESCE(MAX(CASE WHEN at.id IS NOT NULL THEN 1 ELSE 0 END), 0) AS answered_by_learner,
                       MIN(qc.concept_id::text) AS concept_id
                FROM assessment_questions q
                LEFT JOIN question_concepts qc ON qc.question_id = q.id
                LEFT JOIN assessment_answers aa ON aa.question_id = q.id
                LEFT JOIN assessment_attempts at ON at.id = aa.attempt_id AND at.user_id = ?
                WHERE q.assessment_id = ?
                GROUP BY q.id
                ORDER BY q.id
                """, (rs, rowNum) -> new QuestionSignal(
                rs.getObject("id", UUID.class),
                rs.getDouble("success_rate"),
                rs.getLong("learner_attempts"),
                rs.getInt("ever_correct") == 1,
                rs.getTimestamp("last_answered_at") == null ? null : rs.getTimestamp("last_answered_at").toInstant(),
                rs.getInt("answered_by_learner") == 1,
                rs.getString("concept_id") == null ? null : UUID.fromString(rs.getString("concept_id"))),
                userId, assessmentId);
    }

    public record QuestionSignal(UUID questionId, double successRate, long learnerAttempts,
                                 boolean everCorrect, java.time.Instant lastAnsweredAt,
                                 boolean answeredByLearner, UUID conceptId) {}
}
