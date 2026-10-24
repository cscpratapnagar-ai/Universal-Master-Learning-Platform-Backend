package com.masterlearning.platform.modules.assessment.repository;

import com.masterlearning.platform.modules.assessment.entity.AssessmentEvaluatorAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface AssessmentEvaluatorAssignmentRepository extends JpaRepository<AssessmentEvaluatorAssignment, UUID> {
    boolean existsByAssessmentIdAndEvaluatorId(UUID assessmentId, UUID evaluatorId);
    List<AssessmentEvaluatorAssignment> findByAssessmentId(UUID assessmentId);
    List<AssessmentEvaluatorAssignment> findByEvaluatorIdOrderByCreatedAtDesc(UUID evaluatorId);

    @Query("select a from AssessmentEvaluatorAssignment a where a.evaluator.id = :evaluatorId and a.assessment.id = :assessmentId")
    List<AssessmentEvaluatorAssignment> findForEvaluatorAndAssessment(
            @Param("evaluatorId") UUID evaluatorId, @Param("assessmentId") UUID assessmentId);
}
