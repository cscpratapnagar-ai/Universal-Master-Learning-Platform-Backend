package com.masterlearning.platform.modules.ai.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdaptiveTutorContextServiceTest {
    private final AdaptiveTutorContextService service = new AdaptiveTutorContextService();

    @Test
    void normalizesWhitespaceAndNullInput() {
        assertEquals("Explain recursion clearly", service.normalizeQuestion("  Explain   recursion   clearly  "));
        assertEquals("", service.normalizeQuestion(null));
    }

    @Test
    void selectsExplanationStyleFromMastery() {
        assertEquals("SIMPLE_STEP_BY_STEP", service.explanationStyle(49.9));
        assertEquals("BALANCED_EXPLANATION", service.explanationStyle(50.0));
        assertEquals("BALANCED_EXPLANATION", service.explanationStyle(84.9));
        assertEquals("CHALLENGE_AND_ACCELERATE", service.explanationStyle(85.0));
    }
}
