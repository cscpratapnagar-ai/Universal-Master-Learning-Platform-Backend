package com.masterlearning.platform.modules.course.service;

import com.masterlearning.platform.modules.course.dto.response.LearningAnalyticsResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LearningAnalyticsEngineTest {

    private final LearningAnalyticsEngine engine = new LearningAnalyticsEngine();

    @Test
    void noProgressProducesHighRiskStartState() {
        LearningAnalyticsResponse result = engine.analyze(
                UUID.randomUUID(), 0, 10, 0, 0,
                Instant.now().minus(1, ChronoUnit.DAYS), null);

        assertEquals("AT_RISK", result.completionPrediction());
        assertEquals("NOT_STARTED", result.engagementState());
        assertEquals("HIGH", result.riskLevel());
    }

    @Test
    void steadyProgressProducesPositiveTrajectory() {
        LearningAnalyticsResponse result = engine.analyze(
                UUID.randomUUID(), 5, 10, 75, 3600,
                Instant.now().minus(10, ChronoUnit.DAYS), Instant.now());

        assertEquals(50, result.completionPercent());
        assertEquals("ON_TIME", result.completionPrediction());
        assertEquals("ACTIVE", result.engagementState());
        assertEquals("LOW", result.riskLevel());
    }

    @Test
    void inactiveLearnerIsFlagged() {
        LearningAnalyticsResponse result = engine.analyze(
                UUID.randomUUID(), 2, 10, 65, 1800,
                Instant.now().minus(20, ChronoUnit.DAYS),
                Instant.now().minus(10, ChronoUnit.DAYS));

        assertEquals("INACTIVE", result.engagementState());
        assertEquals("HIGH", result.riskLevel());
    }
}
