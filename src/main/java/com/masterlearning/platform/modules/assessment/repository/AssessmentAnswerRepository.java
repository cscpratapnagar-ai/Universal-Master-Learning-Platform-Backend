package com.masterlearning.platform.modules.assessment.repository;

import com.masterlearning.platform.modules.assessment.entity.AssessmentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AssessmentAnswerRepository extends JpaRepository<AssessmentAnswer, UUID> {
    List<AssessmentAnswer> findByAttemptId(UUID attemptId);
}