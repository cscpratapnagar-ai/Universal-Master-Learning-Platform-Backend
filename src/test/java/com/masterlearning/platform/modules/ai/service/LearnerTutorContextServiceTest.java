package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.assessment.entity.Assessment;
import com.masterlearning.platform.modules.assessment.entity.AssessmentAttempt;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LearnerTutorContextServiceTest {

    @Test
    void derivesDistinctWeakAreasFromLatestAttemptPerAssessment() {
        Assessment weak = mock(Assessment.class);
        Assessment improving = mock(Assessment.class);
        when(weak.getId()).thenReturn(UUID.randomUUID());
        when(weak.getTitle()).thenReturn("Java Fundamentals");
        when(improving.getId()).thenReturn(UUID.randomUUID());
        when(improving.getTitle()).thenReturn("Spring Security");

        AssessmentAttempt latestWeak = mock(AssessmentAttempt.class);
        AssessmentAttempt olderWeak = mock(AssessmentAttempt.class);
        AssessmentAttempt strong = mock(AssessmentAttempt.class);
        when(latestWeak.getAssessment()).thenReturn(weak);
        when(latestWeak.getScore()).thenReturn(45);
        when(olderWeak.getAssessment()).thenReturn(weak);
        when(olderWeak.getScore()).thenReturn(20);
        when(strong.getAssessment()).thenReturn(improving);
        when(strong.getScore()).thenReturn(82);

        assertIterableEquals(List.of("Java Fundamentals"),
                LearnerTutorContextService.weakAreas(List.of(latestWeak, olderWeak, strong)));
    }

    @Test
    void returnsFallbackWhenThereIsNoWeakEvidence() {
        Assessment assessment = mock(Assessment.class);
        when(assessment.getId()).thenReturn(UUID.randomUUID());
        when(assessment.getTitle()).thenReturn("Algorithms");
        AssessmentAttempt attempt = mock(AssessmentAttempt.class);
        when(attempt.getAssessment()).thenReturn(assessment);
        when(attempt.getScore()).thenReturn(60);

        assertEquals(List.of("No specific weak area identified"),
                LearnerTutorContextService.weakAreas(List.of(attempt)));
    }

    @Test
    void returnsFallbackForEmptyHistory() {
        assertEquals(List.of("No specific weak area identified"),
                LearnerTutorContextService.weakAreas(List.of()));
    }
}
