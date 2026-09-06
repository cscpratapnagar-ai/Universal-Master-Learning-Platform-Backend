package com.masterlearning.platform.modules.assessment.service;

import com.masterlearning.platform.modules.assessment.entity.Assessment;
import com.masterlearning.platform.modules.assessment.entity.AssessmentAttempt;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AssessmentIntelligenceEngineTest {
    private final AssessmentIntelligenceEngine engine = new AssessmentIntelligenceEngine();

    @Test void noEvidenceProducesNotReady() {
        var r = engine.analyze(UUID.randomUUID(), List.of());
        assertEquals("NOT_READY", r.readiness());
        assertEquals("NO_DATA", r.trend());
        assertFalse(r.retakeRecommended());
    }

    @Test void improvingAttemptsProduceReadySignal() {
        var assessment = org.mockito.Mockito.mock(Assessment.class);
        UUID assessmentId = UUID.randomUUID();
        org.mockito.Mockito.when(assessment.getId()).thenReturn(assessmentId);
        org.mockito.Mockito.when(assessment.getTitle()).thenReturn("Final Assessment");
        org.mockito.Mockito.when(assessment.getAssessmentLevel()).thenReturn("COURSE");
        org.mockito.Mockito.when(assessment.getMaxAttempts()).thenReturn(3);
        var oldAttempt = org.mockito.Mockito.mock(AssessmentAttempt.class);
        var newAttempt = org.mockito.Mockito.mock(AssessmentAttempt.class);
        org.mockito.Mockito.when(oldAttempt.getAssessment()).thenReturn(assessment);
        org.mockito.Mockito.when(newAttempt.getAssessment()).thenReturn(assessment);
        org.mockito.Mockito.when(oldAttempt.getScore()).thenReturn(55);
        org.mockito.Mockito.when(newAttempt.getScore()).thenReturn(85);
        org.mockito.Mockito.when(oldAttempt.isPassed()).thenReturn(false);
        org.mockito.Mockito.when(newAttempt.isPassed()).thenReturn(true);
        org.mockito.Mockito.when(oldAttempt.getSubmittedAt()).thenReturn(Instant.parse("2026-09-01T10:00:00Z"));
        org.mockito.Mockito.when(newAttempt.getSubmittedAt()).thenReturn(Instant.parse("2026-09-02T10:00:00Z"));
        var r = engine.analyze(UUID.randomUUID(), List.of(newAttempt, oldAttempt));
        assertEquals("IMPROVING", r.trend());
        assertEquals("READY", r.readiness());
        assertFalse(r.retakeRecommended());
    }

    @Test void failedLatestAttemptRecommendsRetake() {
        var assessment = org.mockito.Mockito.mock(Assessment.class);
        org.mockito.Mockito.when(assessment.getId()).thenReturn(UUID.randomUUID());
        org.mockito.Mockito.when(assessment.getTitle()).thenReturn("Module Test");
        org.mockito.Mockito.when(assessment.getAssessmentLevel()).thenReturn("MODULE");
        org.mockito.Mockito.when(assessment.getMaxAttempts()).thenReturn(3);
        var attempt = org.mockito.Mockito.mock(AssessmentAttempt.class);
        org.mockito.Mockito.when(attempt.getAssessment()).thenReturn(assessment);
        org.mockito.Mockito.when(attempt.getScore()).thenReturn(45);
        org.mockito.Mockito.when(attempt.isPassed()).thenReturn(false);
        org.mockito.Mockito.when(attempt.getSubmittedAt()).thenReturn(Instant.now());
        var r = engine.analyze(UUID.randomUUID(), List.of(attempt));
        assertTrue(r.retakeRecommended());
        assertEquals("NOT_READY", r.readiness());
    }
}
