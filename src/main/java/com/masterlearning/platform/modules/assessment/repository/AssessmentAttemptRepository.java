package com.masterlearning.platform.modules.assessment.repository;

import com.masterlearning.platform.modules.assessment.entity.AssessmentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, UUID> {
    Optional<AssessmentAttempt> findTopByAssessmentIdAndUserIdAndPassedTrueOrderBySubmittedAtDesc(
            UUID assessmentId, UUID userId);
}