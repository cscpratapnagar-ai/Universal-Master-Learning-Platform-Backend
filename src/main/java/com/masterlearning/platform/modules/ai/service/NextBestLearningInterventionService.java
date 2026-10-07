package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.AiLearningOrchestration;
import com.masterlearning.platform.modules.ai.dto.response.AiKnowledgeContext;
import com.masterlearning.platform.modules.ai.dto.response.NextBestLearningIntervention;
import com.masterlearning.platform.modules.ai.entity.LearningInterventionOutcome;
import com.masterlearning.platform.modules.ai.repository.LearningInterventionOutcomeRepository;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.modules.learning.dto.response.PersonalizedRecommendation;
import com.masterlearning.platform.modules.learning.service.PersonalizedRecommendationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class NextBestLearningInterventionService {
    private final EnrollmentRepository enrollments;
    private final AiLearningOrchestratorService learningOrchestrator;
    private final AiKnowledgeContextService knowledgeContext;
    private final PersonalizedRecommendationService recommendations;
    private final LearningInterventionOutcomeRepository interventionOutcomes;

    public NextBestLearningInterventionService(EnrollmentRepository enrollments,
                                               AiLearningOrchestratorService learningOrchestrator,
                                               AiKnowledgeContextService knowledgeContext,
                                               PersonalizedRecommendationService recommendations,
                                               LearningInterventionOutcomeRepository interventionOutcomes) {
        this.enrollments = enrollments;
        this.learningOrchestrator = learningOrchestrator;
        this.knowledgeContext = knowledgeContext;
        this.recommendations = recommendations;
        this.interventionOutcomes = interventionOutcomes;
    }

    public NextBestLearningIntervention forEnrollment(UUID enrollmentId, UUID userId) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Enrollment does not belong to current user");
        }

        AiLearningOrchestration orchestration = learningOrchestrator.forEnrollment(enrollmentId, userId);
        AiKnowledgeContext knowledge = knowledgeContext.forEnrollment(enrollmentId, userId);
        PersonalizedRecommendation recommendation = recommendations.forEnrollment(enrollmentId, userId);
        List<LearningInterventionOutcome> history = interventionOutcomes.findTop5ByEnrollmentIdOrderByCreatedAtDesc(enrollmentId);

        String intervention;
        String priority;
        String rationale;
        String expectedOutcome;

        if (orchestration.recommendedAction().equals("REMEDIATE")) {
            intervention = knowledge.prerequisiteConcepts().isEmpty()
                    ? "REMEDIATION"
                    : "PREREQUISITE_RETEACH";
            priority = "CRITICAL";
            rationale = "Critical mastery gaps require foundational repair before normal progression";
            expectedOutcome = "Restore prerequisite understanding and raise mastery enough to progress safely";
        } else if (orchestration.recommendedAction().equals("TARGETED_PRACTICE")) {
            intervention = "TARGETED_PRACTICE";
            priority = "HIGH";
            rationale = "Weak concepts are the highest-value immediate learning target";
            expectedOutcome = "Strengthen weak concepts and reduce the learner's knowledge gaps";
        } else if (orchestration.recommendedAction().equals("ACCELERATE")) {
            intervention = "ACCELERATION";
            priority = "MEDIUM";
            rationale = "Strong mastery indicates readiness for deeper or faster progression";
            expectedOutcome = "Increase challenge while preserving mastery and transfer";
        } else if (orchestration.recommendedAction().equals("COURSE_REVIEW")) {
            intervention = "COURSE_REVIEW";
            priority = "LOW";
            rationale = "No active lesson target is available from the course sequence";
            expectedOutcome = "Identify the next valid learning target or course completion state";
        } else {
            intervention = "NEXT_BEST_LESSON";
            priority = orchestration.priority();
            rationale = "The learner is ready for the next appropriate lesson in the personalized sequence";
            expectedOutcome = "Continue progression without unnecessary remediation or overload";
        }

        LearningInterventionOutcome latest = history.isEmpty() ? null : history.get(0);
        String recentOutcome = latest == null ? null : latest.getOutcome();
        boolean repeatRisk = latest != null
                && intervention.equalsIgnoreCase(latest.getInterventionType())
                && ("FAILED".equalsIgnoreCase(latest.getOutcome())
                || "NO_IMPROVEMENT".equalsIgnoreCase(latest.getOutcome()));
        long sameInterventionFailures = history.stream()
                .filter(o -> intervention.equalsIgnoreCase(o.getInterventionType()))
                .filter(o -> "FAILED".equalsIgnoreCase(o.getOutcome()) || "NO_IMPROVEMENT".equalsIgnoreCase(o.getOutcome()))
                .count();
        boolean persistentFailureRisk = sameInterventionFailures >= 2;

        if (persistentFailureRisk) {
            priority = "CRITICAL";
            rationale = "Repeated failure of the same intervention indicates a persistent learning barrier; switch strategy instead of repeating the same treatment";
            expectedOutcome = "Use a materially different teaching or practice strategy and produce measurable improvement";
        } else if (repeatRisk) {
            priority = "CRITICAL";
            rationale = "The same intervention recently failed or produced no improvement; the learner needs a stronger recovery strategy";
            expectedOutcome = "Break the repeated intervention pattern and produce measurable learning improvement";
        }

        List<String> reasons = new ArrayList<>(orchestration.reasons());
        reasons.add("Selected intervention: " + intervention);
        if (!knowledge.weakConcepts().isEmpty()) {
            reasons.add("Knowledge context weak concepts: " + String.join(", ", knowledge.weakConcepts()));
        }
        if (!knowledge.prerequisiteConcepts().isEmpty()) {
            reasons.add("Prerequisite context: " + String.join(", ", knowledge.prerequisiteConcepts()));
        }
        reasons.add("Personalized recommendation: " + recommendation.action());
        if (recentOutcome != null) {
            reasons.add("Recent intervention outcome: " + recentOutcome);
        }
        if (repeatRisk) {
            reasons.add("Repeat-risk detected: the latest matching intervention did not improve the learner outcome");
        }
        if (persistentFailureRisk) {
            reasons.add("Persistent-failure detected: the same intervention has failed or shown no improvement at least twice in recent history");
        }

        return new NextBestLearningIntervention(
                enrollmentId,
                enrollment.getCourse().getId(),
                intervention,
                priority,
                recommendation.targetLessonId(),
                recommendation.targetLessonTitle(),
                rationale,
                expectedOutcome,
                List.copyOf(reasons),
                recentOutcome,
                repeatRisk
        );
    }
}
