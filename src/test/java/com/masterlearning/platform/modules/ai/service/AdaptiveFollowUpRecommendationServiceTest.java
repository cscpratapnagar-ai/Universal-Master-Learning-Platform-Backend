package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.AdaptiveFollowUpRecommendation;
import com.masterlearning.platform.modules.ai.dto.response.LearnerTutorContext;
import com.masterlearning.platform.modules.ai.dto.response.TutorIntelligenceContext;
import com.masterlearning.platform.modules.ai.dto.response.UnderstandingEvaluation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AdaptiveFollowUpRecommendationServiceTest {

    private final AdaptiveFollowUpRecommendationService service = new AdaptiveFollowUpRecommendationService();

    @Test
    void confusedLearnerGetsSocraticScaffolding() {
        AdaptiveFollowUpRecommendation result = service.recommend(
                learner(35.0),
                intelligence(List.of("explicit_confusion")),
                new UnderstandingEvaluation("CONFUSED", "RETEACH_WITH_SCAFFOLDING", true, true));

        assertEquals("SOCRATIC_SCAFFOLD", result.mode());
        assertEquals("GUIDED_STEP", result.questionType());
        assertEquals("EASY", result.difficulty());
        assertTrue(result.required());
    }

    @Test
    void prerequisiteBlockerGetsFoundationCheck() {
        AdaptiveFollowUpRecommendation result = service.recommend(
                learner(72.0),
                intelligence(List.of("prerequisite_blocker")),
                new UnderstandingEvaluation("PARTIAL_OR_UNKNOWN", "CHECK_WITH_TARGETED_QUESTION", false, true));

        assertEquals("PREREQUISITE_CHECK", result.mode());
        assertEquals("FOUNDATION_CHECK", result.questionType());
        assertEquals("EASY", result.difficulty());
        assertTrue(result.required());
    }

    @Test
    void advancedLearnerGetsOptionalChallenge() {
        AdaptiveFollowUpRecommendation result = service.recommend(
                learner(92.0),
                intelligence(List.of()),
                new UnderstandingEvaluation("LIKELY_UNDERSTOOD", "DEEPEN_OR_ADVANCE", false, false));

        assertEquals("CHALLENGE", result.mode());
        assertEquals("TRANSFER", result.questionType());
        assertEquals("HARD", result.difficulty());
        assertFalse(result.required());
    }

    private LearnerTutorContext learner(double mastery) {
        return new LearnerTutorContext(
                UUID.randomUUID(), mastery, "LEARNING", "LOW", "STABLE",
                List.of(), List.of(), "LEARN", "STANDARD");
    }

    private TutorIntelligenceContext intelligence(List<String> signals) {
        return new TutorIntelligenceContext(
                "NONE_DETECTED", "DIRECT_TEACHING", "CHECK_UNDERSTANDING",
                "INTERMEDIATE", true, signals);
    }
}
