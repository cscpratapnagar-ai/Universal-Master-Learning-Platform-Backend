package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.LearnerTutorContext;
import com.masterlearning.platform.modules.ai.dto.response.TargetedPracticeRecommendation;
import com.masterlearning.platform.modules.ai.dto.response.TutorIntelligenceContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TargetedPracticeRecommendationServiceTest {
    private final TargetedPracticeRecommendationService service = new TargetedPracticeRecommendationService();

    private LearnerTutorContext learner(double mastery, List<String> weakConcepts) {
        return new LearnerTutorContext(UUID.randomUUID(), mastery, "LEARNING", "MEDIUM", "BUILDING", List.of(), weakConcepts, "LEARN", "BALANCED_EXPLANATION");
    }

    @Test
    void prioritizesPrerequisitePractice() {
        TutorIntelligenceContext intelligence = new TutorIntelligenceContext("NONE_DETECTED", "PREREQUISITE_BRIDGE", "CHECK_UNDERSTANDING", "INTERMEDIATE", true, List.of("prerequisite_blocker"));
        TargetedPracticeRecommendation result = service.recommend(learner(70, List.of("Target")), intelligence);

        assertEquals("HIGH", result.priority());
        assertEquals("PREREQUISITE_PRACTICE", result.practiceType());
        assertTrue(result.recommended());
    }

    @Test
    void usesScaffoldedPracticeForFoundationalLearner() {
        TutorIntelligenceContext intelligence = new TutorIntelligenceContext("NONE_DETECTED", "SCAFFOLDED_EXPLANATION", "CHECK_UNDERSTANDING", "FOUNDATIONAL", true, List.of());
        TargetedPracticeRecommendation result = service.recommend(learner(35, List.of("Fractions")), intelligence);

        assertEquals("HIGH", result.priority());
        assertEquals("SCAFFOLDED_PRACTICE", result.practiceType());
        assertEquals("EASY", result.difficulty());
        assertEquals("Fractions", result.focus());
    }

    @Test
    void avoidsPracticeWhenAdvancedLearnerDoesNotNeedIt() {
        TutorIntelligenceContext intelligence = new TutorIntelligenceContext("NONE_DETECTED", "DEEPEN_AND_CHALLENGE", "OPTIONAL_CHALLENGE", "ADVANCED", false, List.of());
        TargetedPracticeRecommendation result = service.recommend(learner(92, List.of()), intelligence);

        assertFalse(result.recommended());
        assertEquals("CHALLENGE", result.practiceType());
        assertEquals("HARD", result.difficulty());
    }
}
