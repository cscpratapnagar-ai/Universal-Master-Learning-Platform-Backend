package com.masterlearning.platform.modules.ai.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnderstandingEvaluationServiceTest {
    private final UnderstandingEvaluationService service = new UnderstandingEvaluationService();

    @Test
    void confusionTriggersReteachingAndPractice() {
        var result = service.evaluate("I still don't understand this");
        assertEquals("CONFUSED", result.signal());
        assertEquals("RETEACH_WITH_SCAFFOLDING", result.nextAction());
        assertTrue(result.needsRetry());
        assertTrue(result.needsPractice());
    }

    @Test
    void uncertaintyTriggersTargetedCheck() {
        var result = service.evaluate("I think I understand, but I am not sure");
        assertEquals("UNCERTAIN", result.signal());
        assertEquals("CHECK_WITH_TARGETED_QUESTION", result.nextAction());
        assertTrue(result.needsRetry());
    }

    @Test
    void positiveUnderstandingAllowsAdvancement() {
        var result = service.evaluate("Got it, that makes sense");
        assertEquals("LIKELY_UNDERSTOOD", result.signal());
        assertEquals("DEEPEN_OR_ADVANCE", result.nextAction());
        assertFalse(result.needsRetry());
    }

    @Test
    void practiceRequestProducesTargetedPractice() {
        var result = service.evaluate("Give me one practice question");
        assertEquals("ENGAGED", result.signal());
        assertEquals("GIVE_TARGETED_PRACTICE", result.nextAction());
        assertTrue(result.needsPractice());
    }
}
