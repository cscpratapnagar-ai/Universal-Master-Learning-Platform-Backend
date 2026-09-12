package com.masterlearning.platform.modules.ai.repository;

import com.masterlearning.platform.modules.ai.entity.LearningConcept;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearningConceptRepository extends JpaRepository<LearningConcept, UUID> {
    List<LearningConcept> findByCourseIdAndActiveTrueOrderByNameAsc(UUID courseId);
    Optional<LearningConcept> findByCourseIdAndCode(UUID courseId, String code);
}
