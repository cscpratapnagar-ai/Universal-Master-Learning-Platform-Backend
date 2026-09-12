package com.masterlearning.platform.modules.ai.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ConceptMasteryService {
    private final JdbcTemplate jdbcTemplate;

    public ConceptMasteryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ConceptMastery> forLearner(UUID courseId, UUID userId) {
        return jdbcTemplate.query(
                "SELECT c.id, c.name, c.concept_type, " +
                        "ROUND(COALESCE(100.0 * SUM(CASE WHEN aa.correct THEN 1 ELSE 0 END) / NULLIF(COUNT(aa.id), 0), 0), 2) AS mastery, " +
                        "COUNT(aa.id) AS evidence_count " +
                        "FROM learning_concepts c " +
                        "JOIN question_concepts qc ON qc.concept_id = c.id " +
                        "JOIN assessment_questions q ON q.id = qc.question_id " +
                        "LEFT JOIN assessment_answers aa ON aa.question_id = q.id " +
                        "LEFT JOIN assessment_attempts at ON at.id = aa.attempt_id AND at.user_id = ? " +
                        "WHERE c.course_id = ? AND c.active = TRUE " +
                        "GROUP BY c.id, c.name, c.concept_type " +
                        "ORDER BY mastery ASC, c.name ASC",
                (rs, rowNum) -> new ConceptMastery(
                        rs.getObject("id", UUID.class),
                        rs.getString("name"),
                        rs.getString("concept_type"),
                        rs.getDouble("mastery"),
                        rs.getLong("evidence_count")),
                userId, courseId);
    }

    public List<String> weakConcepts(UUID courseId, UUID userId, double threshold, int limit) {
        return forLearner(courseId, userId).stream()
                .filter(c -> c.evidenceCount() > 0 && c.mastery() < threshold)
                .limit(Math.max(1, limit))
                .map(ConceptMastery::name)
                .toList();
    }

    public record ConceptMastery(UUID conceptId, String name, String type, double mastery, long evidenceCount) {}
}
