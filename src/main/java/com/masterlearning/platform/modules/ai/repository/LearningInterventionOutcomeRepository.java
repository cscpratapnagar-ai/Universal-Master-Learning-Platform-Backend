package com.masterlearning.platform.modules.ai.repository;

import com.masterlearning.platform.modules.ai.entity.LearningInterventionOutcome;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LearningInterventionOutcomeRepository extends JpaRepository<LearningInterventionOutcome, UUID> {
    List<LearningInterventionOutcome> findTop20ByEnrollmentIdOrderByCreatedAtDesc(UUID enrollmentId);

    List<LearningInterventionOutcome> findTop5ByEnrollmentIdOrderByCreatedAtDesc(UUID enrollmentId);
}
