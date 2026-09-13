package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.AdaptiveTeachingDecision;
import com.masterlearning.platform.modules.ai.dto.response.AiKnowledgeContext;
import com.masterlearning.platform.modules.ai.dto.response.AiTutorOrchestration;
import com.masterlearning.platform.modules.ai.dto.response.LearnerTutorContext;
import com.masterlearning.platform.modules.ai.dto.response.TutorIntelligenceContext;
import com.masterlearning.platform.modules.ai.dto.response.UnderstandingEvaluation;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AiTutorOrchestrationService {
    private final EnrollmentRepository enrollments;
    private final LearnerTutorContextService learnerContext;
    private final AiKnowledgeContextService knowledgeContext;
    private final TutorIntelligenceService intelligence;
    private final AdaptiveTeachingDecisionService teachingDecision;

    public AiTutorOrchestrationService(EnrollmentRepository enrollments,
                                       LearnerTutorContextService learnerContext,
                                       AiKnowledgeContextService knowledgeContext,
                                       TutorIntelligenceService intelligence,
                                       AdaptiveTeachingDecisionService teachingDecision) {
        this.enrollments = enrollments;
        this.learnerContext = learnerContext;
        this.knowledgeContext = knowledgeContext;
        this.intelligence = intelligence;
        this.teachingDecision = teachingDecision;
    }

    public AiTutorOrchestration decide(UUID enrollmentId, UUID userId, String question) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Enrollment does not belong to current user");
        }

        LearnerTutorContext learner = learnerContext.build(enrollmentId);
        AiKnowledgeContext knowledge = knowledgeContext.forEnrollment(enrollmentId, userId);
        boolean prerequisiteBlocked = !knowledge.prerequisiteConcepts().isEmpty();
        TutorIntelligenceContext tutorSignals = intelligence.analyze(
                question == null ? "" : question, learner, prerequisiteBlocked);
        UnderstandingEvaluation understanding = null;
        AdaptiveTeachingDecision decision = teachingDecision.decide(learner, tutorSignals, understanding);

        List<String> reasons = new ArrayList<>(decision.reasons());
        if (!knowledge.weakConcepts().isEmpty()) reasons.add("weak_concept_context");
        if (!knowledge.prerequisiteConcepts().isEmpty()) reasons.add("prerequisite_context");
        if (!knowledge.relevantNodes().isEmpty()) reasons.add("knowledge_graph_context");

        return new AiTutorOrchestration(
                enrollmentId,
                enrollment.getCourse().getId(),
                learner.learnerState(),
                decision.intervention(),
                decision.teachingStrategy(),
                decision.explanationLevel(),
                decision.difficulty(),
                decision.followUpMode(),
                decision.practiceRecommended(),
                decision.prerequisiteBlocked(),
                List.copyOf(reasons)
        );
    }
}
