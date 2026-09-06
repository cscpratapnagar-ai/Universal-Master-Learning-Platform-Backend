package com.masterlearning.platform.modules.course.service;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class DifficultyAdaptationEngineTest {
    private final DifficultyAdaptationEngine engine = new DifficultyAdaptationEngine();

    @Test void repeatedFailuresDecreaseDifficulty() {
        var r = engine.decide(UUID.randomUUID(), 65, 0, 2);
        assertEquals("EASY", r.difficulty());
        assertEquals("DECREASE", r.direction());
    }

    @Test void repeatedSuccessIncreasesDifficulty() {
        var r = engine.decide(UUID.randomUUID(), 75, 3, 0);
        assertEquals("HARD", r.difficulty());
        assertEquals("INCREASE", r.direction());
    }

    @Test void stablePerformanceHoldsDifficulty() {
        var r = engine.decide(UUID.randomUUID(), 70, 1, 1);
        assertEquals("MEDIUM", r.difficulty());
        assertEquals("HOLD", r.direction());
    }
}
