package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.AdaptiveAssessmentOrchestration;
import com.masterlearning.platform.modules.ai.dto.response.AiKnowledgeContext;
import com.masterlearning.platform.modules.ai.dto.response.AiLearningOrchestration;
import com.masterlearning.platform.modules.ai.dto.response.LearnerTutorContext;
import com.masterlearning.platform.modules.assessment.entity.AssessmentAttempt;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAttemptRepository;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AdaptiveAssessmentOrchestrationService {
    private final EnrollmentRepository enrollments;
    private final AssessmentAttemptRepository attempts;
    private final LearnerTutorContextService learnerContext;
    private final AiKnowledgeContextService knowledgeContext;
    private final AiLearningOrchestratorService learningOrchestrator;

    public AdaptiveAssessmentOrchestrationService(EnrollmentRepository enrollments,
                                                  AssessmentAttemptRepository attempts,
                                                  LearnerTutorContextService learnerContext,
                                                  AiKnowledgeContextService knowledgeContext,
                                                  AiLearningOrchestratorService learningOrchestrator) {
        this.enrollments = enrollments;
        this.attempts = attempts;
        this.learnerContext = learnerContext;
        this.knowledgeContext = knowledgeContext;
        this.learningOrchestrator = learningOrchestrator;
    }

    public AdaptiveAssessmentOrchestration decide(UUID enrollmentId, UUID userId) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Enrollment does not belong to current user");
        }

        LearnerTutorContext learner = learnerContext.build(enrollmentId);
        AiKnowledgeContext knowledge = knowledgeContext.forEnrollment(enrollmentId, userId);
        AiLearningOrchestration overall = learningOrchestrator.forEnrollment(enrollmentId, userId);
        List<AssessmentAttempt> history = attempts.findByCourseIdAndUserIdOrderBySubmittedAtDesc(
                enrollment.getCourse().getId(), userId);

        String readiness;
        String action;
        String difficulty;
        String focus;
        boolean recommended;
        List<String> reasons = new ArrayList<>();

        if (history.isEmpty()) {
            readiness = "NOT_READY";
            action = "DIAGNOSTIC_ASSESSMENT";
            difficulty = "MEDIUM";
            focus = knowledge.weakConcepts().isEmpty() ? "FOUNDATION" : String.join(", ", knowledge.weakConcepts());
            recommended = true;
            reasons.add("no_assessment_history");
        } else if (!knowledge.weakConcepts().isEmpty() || learner.masteryScore() < 50.0) {
            readiness = "NOT_READY";
            action = "TARGETED_REASSESSMENT";
            difficulty = "EASY";
            focus = String.join(", ", knowledge.weakConcepts());
            recommended = true;
            reasons.add("weak_concept_or_low_mastery");
        } else if (learner.masteryScore() >= 85.0) {
            readiness = "READY";
            action = "CHALLENGE_ASSESSMENT";
            difficulty = "HARD";
            focus = "TRANSFER_AND_MASTERY";
            recommended = true;
            reasons.add("advanced_mastery");
        } else if (learner.masteryScore() >= 70.0) {
            readiness = "READY";
            action = "MASTERY_ASSESSMENT";
            difficulty = "MEDIUM";
            focus = "MASTERY_CHECK";
            recommended = true;
            reasons.add("sufficient_mastery");
        } else {
            readiness = "NEAR_READY";
            action = "PRACTICE_BEFORE_ASSESSMENT";
            difficulty = "MEDIUM";
            focus = "TARGETED_PRACTICE";
            recommended = false;
            reasons.add("developing_mastery");
        }

        reasons.add("overall_learning_action:" + overall.recommendedAction());
        if (!knowledge.prerequisiteConcepts().isEmpty()) reasons.add("prerequisite_context");
        return new AdaptiveAssessmentOrchestration(enrollmentId, enrollment.getCourse().getId(),
                readiness, action, difficulty, focus, recommended, List.copyOf(reasons));
    }
}
