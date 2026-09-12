package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LearningStateInterventionServiceTest {

    private final LearningStateInterventionService service = new LearningStateInterventionService();

    @Test
    void confusionPrioritizesReteach() {
        var learner = learner(45.0);
        var intelligence = intelligence("SOCRATIC_GUIDANCE", List.of("explicit_confusion"));
        var explanation = new AdaptiveExplanationPlan("FOUNDATIONAL", "GUIDED", "STEP_BY_STEP", true, true, true, List.of());
        var practice = new TargetedPracticeRecommendation("HIGH", "SCAFFOLDED_PRACTICE", "EASY", "CURRENT_CONCEPT", true, List.of());
        var understanding = new UnderstandingEvaluation("CONFUSED", "RETEACH_WITH_SCAFFOLDING", true, true);

        var result = service.decide(learner, intelligence, explanation, practice, understanding);

        assertEquals("RETEACH_AND_CHECK", result.intervention());
        assertTrue(result.practiceRecommended());
    }

    @Test
    void prerequisiteGetsBridgeIntervention() {
        var learner = learner(65.0);
        var intelligence = intelligence("PREREQUISITE_BRIDGE", List.of("prerequisite_blocker"));
        var explanation = new AdaptiveExplanationPlan("FOUNDATIONAL", "GUIDED", "STEP_BY_STEP", true, true, true, List.of());
        var practice = new TargetedPracticeRecommendation("HIGH", "PREREQUISITE_PRACTICE", "MEDIUM", "PREREQUISITE_CONCEPT", true, List.of());
        var understanding = new UnderstandingEvaluation("PARTIAL_OR_UNKNOWN", "CHECK_WITH_TARGETED_QUESTION", false, true);

        var result = service.decide(learner, intelligence, explanation, practice, understanding);

        assertEquals("BRIDGE_PREREQUISITE", result.intervention());
        assertEquals("FOUNDATIONAL", result.explanationLevel());
    }

    @Test
    void advancedLearnerGetsChallengeIntervention() {
        var learner = learner(92.0);
        var intelligence = intelligence("DEEPEN_AND_CHALLENGE", List.of());
        var explanation = new AdaptiveExplanationPlan("ADVANCED", "DEEP", "CONCEPT_THEN_APPLICATION", false, false, false, List.of());
        var practice = new TargetedPracticeRecommendation("LOW", "CHALLENGE", "HARD", "ADVANCEMENT", false, List.of());
        var understanding = new UnderstandingEvaluation("LIKELY_UNDERSTOOD", "DEEPEN_OR_ADVANCE", false, false);

        var result = service.decide(learner, intelligence, explanation, practice, understanding);

        assertEquals("DEEPEN_AND_CHALLENGE", result.intervention());
        assertEquals("ADVANCED", result.explanationLevel());
        assertFalse(result.practiceRecommended());
    }

    private LearnerTutorContext learner(double mastery) {
        return new LearnerTutorContext(UUID.randomUUID(), mastery, "LEARNING", "LOW", "STABLE", List.of(), List.of(), "LEARN", "BALANCED");
    }

    private TutorIntelligenceContext intelligence(String strategy, List<String> signals) {
        return new TutorIntelligenceContext("NONE_DETECTED", strategy, "CHECK_UNDERSTANDING", "INTERMEDIATE", true, signals);
    }
}
