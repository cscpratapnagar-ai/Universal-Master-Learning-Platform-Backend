package com.masterlearning.platform.modules.course.service;

import com.masterlearning.platform.modules.course.entity.Lesson;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NextBestLearningEngineTest {
    private final NextBestLearningEngine engine = new NextBestLearningEngine();

    private Lesson lesson(String title) {
        Lesson lesson = mock(Lesson.class);
        when(lesson.getId()).thenReturn(UUID.randomUUID());
        when(lesson.getTitle()).thenReturn(title);
        return lesson;
    }

    @Test void recommendsRemediationFirst() {
        Lesson weak = lesson("Weak Lesson");
        Lesson normal = lesson("Normal Lesson");
        var result = engine.recommend(UUID.randomUUID(), List.of(normal, weak), List.of(), Set.of(),
                Set.of(weak.getId()), 70);
        assertEquals("REMEDIATION", result.recommendationType());
        assertEquals(weak.getId(), result.recommendedLessonId());
    }

    @Test void strongLearnerGetsAcceleration() {
        Lesson first = lesson("Lesson 1");
        var result = engine.recommend(UUID.randomUUID(), List.of(first), List.of(), Set.of(), Set.of(), 90);
        assertEquals("ACCELERATION", result.recommendationType());
        assertEquals("ACCELERATED", result.learnerState());
    }

    @Test void completedCourseReturnsNone() {
        Lesson first = lesson("Lesson 1");
        var result = engine.recommend(UUID.randomUUID(), List.of(first), List.of(), Set.of(first.getId()), Set.of(), 90);
        assertEquals("NONE", result.recommendationType());
        assertEquals("COMPLETE", result.learnerState());
    }
}
