package com.masterlearning.platform.modules.assessment.service;

import com.masterlearning.platform.modules.assessment.repository.AssessmentAnswerRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KnowledgeGapEngineTest {

    private final KnowledgeGapEngine engine = new KnowledgeGapEngine();

    @Test
    void returnsNoDataWhenEvidenceIsEmpty() {
        var result = engine.analyze(List.of());
        assertEquals(0, result.totalEvaluatedQuestions());
        assertEquals("NO_DATA", result.overallStatus());
    }

    @Test
    void detectsAndPrioritizesWeakQuestions() {
        var weak = projection(UUID.randomUUID(), false, 0, 1, 100);
        var strong = projection(UUID.randomUUID(), true, 1, 1, 100);

        var result = engine.analyze(List.of(strong, weak));

        assertEquals(2, result.totalEvaluatedQuestions());
        assertEquals(1, result.weakQuestions());
        assertEquals(50.0, result.averageAccuracyPercent());
        assertEquals("AT_RISK", result.overallStatus());
        assertEquals("HIGH", result.gaps().getFirst().remediationPriority());
        assertEquals(100.0, result.gaps().getFirst().gapSeverity());
    }

    @Test
    void usesLatestEvidencePerQuestion() {
        UUID questionId = UUID.randomUUID();
        var olderWeak = projection(questionId, false, 0, 1, 50);
        var newerStrong = projection(questionId, true, 1, 1, 100);

        var result = engine.analyze(List.of(newerStrong, olderWeak));

        assertEquals(1, result.totalEvaluatedQuestions());
        assertEquals(0, result.weakQuestions());
        assertEquals(100.0, result.averageAccuracyPercent());
        assertTrue(result.gaps().isEmpty());
    }

    private AssessmentAnswerRepository.KnowledgeGapProjection projection(
            UUID id, boolean correct, int awarded, int max, long submittedSeconds) {
        var p = mock(AssessmentAnswerRepository.KnowledgeGapProjection.class);
        when(p.getQuestionId()).thenReturn(id);
        when(p.getQuestionText()).thenReturn("Test question");
        when(p.getAssessmentTitle()).thenReturn("Test assessment");
        when(p.getLessonId()).thenReturn(UUID.randomUUID());
        when(p.isCorrect()).thenReturn(correct);
        when(p.getPointsAwarded()).thenReturn(awarded);
        when(p.getMaxPoints()).thenReturn(max);
        when(p.getSubmittedAt()).thenReturn(Instant.ofEpochSecond(submittedSeconds));
        return p;
    }
}
