package com.masterlearning.platform.modules.ai.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class LearningKnowledgeGraphRepository {
    private final JdbcTemplate jdbcTemplate;

    public LearningKnowledgeGraphRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<NodeRow> findCourseNodes(UUID courseId) {
        return jdbcTemplate.query("""
                SELECT id, name, concept_type
                FROM learning_concepts
                WHERE course_id = ? AND active = TRUE
                ORDER BY name ASC
                """, (rs, rowNum) -> new NodeRow(
                rs.getObject("id", UUID.class), rs.getString("name"), rs.getString("concept_type")), courseId);
    }

    public List<EdgeRow> findCourseEdges(UUID courseId) {
        return jdbcTemplate.query("""
                SELECT source_concept_id, target_concept_id, relation_type, weight
                FROM learning_concept_relations
                WHERE course_id = ? AND active = TRUE
                UNION ALL
                SELECT p.concept_id, p.prerequisite_concept_id, 'PREREQUISITE', 1.000
                FROM learning_concept_prerequisites p
                JOIN learning_concepts c ON c.id = p.concept_id
                WHERE c.course_id = ?
                ORDER BY source_concept_id, target_concept_id
                """, (rs, rowNum) -> new EdgeRow(
                rs.getObject("source_concept_id", UUID.class),
                rs.getObject("target_concept_id", UUID.class),
                rs.getString("relation_type"),
                rs.getDouble("weight")), courseId, courseId);
    }

    public List<MasteryRow> findLearnerMastery(UUID courseId, UUID userId) {
        return jdbcTemplate.query("""
                SELECT c.id,
                       ROUND(COALESCE(100.0 * SUM(CASE WHEN la.correct THEN 1 ELSE 0 END)
                           / NULLIF(COUNT(la.question_id), 0), 0), 2) AS mastery,
                       COUNT(la.question_id) AS evidence_count
                FROM learning_concepts c
                LEFT JOIN question_concepts qc ON qc.concept_id = c.id
                LEFT JOIN (
                    SELECT aa.question_id, aa.correct
                    FROM assessment_answers aa
                    JOIN assessment_attempts at ON at.id = aa.attempt_id
                    WHERE at.user_id = ?
                ) la ON la.question_id = qc.question_id
                WHERE c.course_id = ? AND c.active = TRUE
                GROUP BY c.id
                ORDER BY c.id
                """, (rs, rowNum) -> new MasteryRow(
                rs.getObject("id", UUID.class),
                rs.getDouble("mastery"),
                rs.getLong("evidence_count")), userId, courseId);
    }

    public record NodeRow(UUID id, String name, String type) {}
    public record EdgeRow(UUID sourceId, UUID targetId, String relationType, double weight) {}
    public record MasteryRow(UUID id, double mastery, long evidenceCount) {}
}
