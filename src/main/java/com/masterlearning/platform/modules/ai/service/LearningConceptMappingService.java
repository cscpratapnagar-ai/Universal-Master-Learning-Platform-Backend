package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.entity.LearningConcept;
import com.masterlearning.platform.modules.ai.repository.LearningConceptMappingRepository;
import com.masterlearning.platform.modules.ai.repository.LearningConceptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LearningConceptMappingService {
    private final LearningConceptRepository concepts;
    private final LearningConceptMappingRepository mappings;

    public LearningConceptMappingService(LearningConceptRepository concepts,
                                         LearningConceptMappingRepository mappings) {
        this.concepts = concepts;
        this.mappings = mappings;
    }

    @Transactional
    public void mapLesson(UUID lessonId, UUID courseId, List<UUID> conceptIds) {
        validateConcepts(courseId, conceptIds);
        mappings.replaceLessonConcepts(lessonId, conceptIds);
    }

    @Transactional
    public void mapQuestion(UUID questionId, UUID courseId, List<UUID> conceptIds) {
        validateConcepts(courseId, conceptIds);
        mappings.replaceQuestionConcepts(questionId, conceptIds);
    }

    public List<LearningConcept> conceptsForCourse(UUID courseId) {
        return concepts.findByCourseIdAndActiveTrueOrderByNameAsc(courseId);
    }

    public List<UUID> conceptIdsForLesson(UUID lessonId) {
        return mappings.findConceptIdsForLesson(lessonId);
    }

    public List<UUID> conceptIdsForQuestion(UUID questionId) {
        return mappings.findConceptIdsForQuestion(questionId);
    }

    private void validateConcepts(UUID courseId, List<UUID> conceptIds) {
        if (conceptIds == null || conceptIds.isEmpty()) return;
        for (UUID conceptId : conceptIds) {
            LearningConcept concept = concepts.findById(conceptId)
                    .orElseThrow(() -> new IllegalArgumentException("Learning concept not found: " + conceptId));
            if (!concept.getCourse().getId().equals(courseId)) {
                throw new IllegalArgumentException("Learning concept belongs to a different course: " + conceptId);
            }
            if (!concept.isActive()) {
                throw new IllegalArgumentException("Learning concept is inactive: " + conceptId);
            }
        }
    }
}
