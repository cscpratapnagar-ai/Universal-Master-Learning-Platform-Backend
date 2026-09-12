package com.masterlearning.platform.modules.ai.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class LearningConceptMappingRepository {
    private final JdbcTemplate jdbcTemplate;

    public LearningConceptMappingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void replaceLessonConcepts(UUID lessonId, List<UUID> conceptIds) {
        jdbcTemplate.update("DELETE FROM lesson_concepts WHERE lesson_id = ?", lessonId);
        if (conceptIds == null) return;
        for (UUID conceptId : conceptIds) {
            jdbcTemplate.update("INSERT INTO lesson_concepts (lesson_id, concept_id) VALUES (?, ?)", lessonId, conceptId);
        }
    }

    public void replaceQuestionConcepts(UUID questionId, List<UUID> conceptIds) {
        jdbcTemplate.update("DELETE FROM question_concepts WHERE question_id = ?", questionId);
        if (conceptIds == null) return;
        for (UUID conceptId : conceptIds) {
            jdbcTemplate.update("INSERT INTO question_concepts (question_id, concept_id) VALUES (?, ?)", questionId, conceptId);
        }
    }

    public List<UUID> findConceptIdsForLesson(UUID lessonId) {
        return jdbcTemplate.query("SELECT concept_id FROM lesson_concepts WHERE lesson_id = ? ORDER BY concept_id",
                (rs, rowNum) -> rs.getObject("concept_id", UUID.class), lessonId);
    }

    public List<UUID> findConceptIdsForQuestion(UUID questionId) {
        return jdbcTemplate.query("SELECT concept_id FROM question_concepts WHERE question_id = ? ORDER BY concept_id",
                (rs, rowNum) -> rs.getObject("concept_id", UUID.class), questionId);
    }

    public Map<UUID, List<String>> findConceptNamesForLessons(List<UUID> lessonIds) {
        if (lessonIds == null || lessonIds.isEmpty()) return Map.of();
        String placeholders = String.join(",", lessonIds.stream().map(id -> "?").toList());
        String sql = "SELECT lc.lesson_id, c.name FROM lesson_concepts lc "
                + "JOIN learning_concepts c ON c.id = lc.concept_id "
                + "WHERE lc.lesson_id IN (" + placeholders + ") AND c.active = TRUE ORDER BY c.name ASC";
        List<Object> args = new ArrayList<>(lessonIds);
        Map<UUID, List<String>> result = new LinkedHashMap<>();
        jdbcTemplate.query(sql, rs -> {
            UUID lessonId = rs.getObject("lesson_id", UUID.class);
            result.computeIfAbsent(lessonId, ignored -> new ArrayList<>()).add(rs.getString("name"));
        }, args.toArray());
        return result;
    }

    public Map<UUID, List<String>> findConceptHierarchyNamesForLessons(List<UUID> lessonIds) {
        if (lessonIds == null || lessonIds.isEmpty()) return Map.of();
        String placeholders = String.join(",", lessonIds.stream().map(id -> "?").toList());
        String sql = "WITH RECURSIVE hierarchy AS ("
                + " SELECT lc.lesson_id, c.id, c.parent_id, c.name FROM lesson_concepts lc "
                + " JOIN learning_concepts c ON c.id = lc.concept_id "
                + " WHERE lc.lesson_id IN (" + placeholders + ") AND c.active = TRUE "
                + " UNION ALL "
                + " SELECT h.lesson_id, p.id, p.parent_id, p.name FROM hierarchy h "
                + " JOIN learning_concepts p ON p.id = h.parent_id WHERE p.active = TRUE"
                + ") SELECT lesson_id, name FROM hierarchy ORDER BY name ASC";
        List<Object> args = new ArrayList<>(lessonIds);
        Map<UUID, List<String>> result = new LinkedHashMap<>();
        jdbcTemplate.query(sql, rs -> {
            UUID lessonId = rs.getObject("lesson_id", UUID.class);
            result.computeIfAbsent(lessonId, ignored -> new ArrayList<>()).add(rs.getString("name"));
        }, args.toArray());
        return result;
    }
}
