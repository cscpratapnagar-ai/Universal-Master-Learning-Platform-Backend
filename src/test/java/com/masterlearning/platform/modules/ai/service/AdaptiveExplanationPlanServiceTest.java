package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.LearnerTutorContext;
import com.masterlearning.platform.modules.ai.dto.response.TutorIntelligenceContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AdaptiveExplanationPlanServiceTest {

    private final AdaptiveExplanationPlanService service = new AdaptiveExplanationPlanService();

    @Test
    void usesFoundationalPlanForPrerequisiteBlocker() {
        var learner = context(40.0);
        var intelligence = new TutorIntelligenceContext("NONE_DETECTED", "PREREQUISITE_BRIDGE", "CHECK_UNDERSTANDING", "INTERMEDIATE", true, List.of("prerequisite_blocker"));

        var result = service.plan(learner, intelligence);

        assertEquals("FOUNDATIONAL", result.level());
        assertTrue(result.useStepByStep());
        assertTrue(result.useExample());
    }

    @Test
    void usesGuidedPlanForMisconceptionRepair() {
        var learner = context(65.0);
        var intelligence = new TutorIntelligenceContext("POSSIBLE", "MISCONCEPTION_REPAIR", "CHECK_UNDERSTANDING", "INTERMEDIATE", true, List.of("possible_misconception"));

        var result = service.plan(learner, intelligence);

        assertEquals("INTERMEDIATE", result.level());
        assertEquals("GUIDED", result.depth());
        assertTrue(result.useExample());
    }

    @Test
    void usesAdvancedPlanForHighMasteryLearner() {
        var learner = context(90.0);
        var intelligence = new TutorIntelligenceContext("NONE_DETECTED", "DEEPEN_AND_CHALLENGE", "OPTIONAL_CHALLENGE", "ADVANCED", false, List.of());

        var result = service.plan(learner, intelligence);

        assertEquals("ADVANCED", result.level());
        assertEquals("DEEP", result.depth());
        assertFalse(result.useStepByStep());
    }

    private LearnerTutorContext context(double mastery) {
        return new LearnerTutorContext(UUID.randomUUID(), mastery, "LEARNING", "LOW", "STABLE", List.of(), List.of(), "LEARN", "BALANCED");
    }
}
