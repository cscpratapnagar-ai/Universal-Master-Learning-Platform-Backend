package com.masterlearning.platform.modules.ai.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
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
}
