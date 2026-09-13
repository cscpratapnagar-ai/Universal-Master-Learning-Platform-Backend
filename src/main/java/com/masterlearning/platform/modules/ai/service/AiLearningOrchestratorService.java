package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.AiLearningOrchestration;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.modules.learning.dto.response.PersonalizedRecommendation;
import com.masterlearning.platform.modules.learning.service.PersonalizedRecommendationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AiLearningOrchestratorService {
    private final EnrollmentRepository enrollments;
    private final LearnerTutorContextService learnerContext;
    private final PersonalizedRecommendationService recommendations;

    public AiLearningOrchestratorService(EnrollmentRepository enrollments,
                                          LearnerTutorContextService learnerContext,
                                          PersonalizedRecommendationService recommendations) {
        this.enrollments = enrollments;
        this.learnerContext = learnerContext;
        this.recommendations = recommendations;
    }

    public AiLearningOrchestration forEnrollment(UUID enrollmentId, UUID userId) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Enrollment does not belong to current user");
        }

        var context = learnerContext.build(enrollmentId);
        PersonalizedRecommendation recommendation = recommendations.forEnrollment(enrollmentId, userId);

        String teachingStrategy = switch (context.learnerState()) {
            case "FOUNDATIONAL" -> "SCAFFOLDED_EXPLANATION";
            case "ADVANCED" -> "DEEPEN_AND_CHALLENGE";
            default -> context.weakConcepts().isEmpty() ? "DIRECT_TEACHING" : "TARGETED_REINFORCEMENT";
        };

        String assessmentAction = switch (recommendation.action()) {
            case "REMEDIATE" -> "RETEACH_AND_REASSESS";
            case "TARGETED_PRACTICE" -> "TARGETED_PRACTICE";
            case "ACCELERATE" -> "CHALLENGE_ASSESSMENT";
            default -> "CONTINUE_LEARNING";
        };

        List<String> reasons = new ArrayList<>();
        reasons.add("Learner state: " + context.learnerState());
        reasons.add("Mastery action: " + context.recommendedAction());
        reasons.add("Recommendation: " + recommendation.action());
        reasons.add("Risk: " + context.riskLevel());
        reasons.add("Momentum: " + context.momentum());
        if (!context.weakConcepts().isEmpty()) {
            reasons.add("Weak concepts: " + String.join(", ", context.weakConcepts()));
        }

        return new AiLearningOrchestration(
                enrollmentId,
                enrollment.getCourse().getId(),
                context.learnerState(),
                recommendation.action(),
                teachingStrategy,
                assessmentAction,
                recommendation.priority(),
                List.copyOf(reasons)
        );
    }
}
