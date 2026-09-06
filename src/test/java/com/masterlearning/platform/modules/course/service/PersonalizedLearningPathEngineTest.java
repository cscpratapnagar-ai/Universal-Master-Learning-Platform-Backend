package com.masterlearning.platform.modules.course.service;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PersonalizedLearningPathEngineTest {
    private final PersonalizedLearningPathEngine engine = new PersonalizedLearningPathEngine();

    @Test void emptyCourseIsComplete() {
        var r = engine.build(UUID.randomUUID(), List.of(), List.of(), Set.of(), 0);
        assertEquals("COMPLETE", r.learnerState());
        assertEquals(0, r.remainingLessons());
    }

    @Test void lowMasteryCreatesRemediationPath() {
        var lesson = org.mockito.Mockito.mock(com.masterlearning.platform.modules.course.entity.Lesson.class);
        UUID id = UUID.randomUUID();
        org.mockito.Mockito.when(lesson.getId()).thenReturn(id);
        org.mockito.Mockito.when(lesson.getTitle()).thenReturn("Lesson 1");
        var r = engine.build(UUID.randomUUID(), List.of(lesson), List.of(), Set.of(), 30);
        assertEquals("REMEDIATION", r.learnerState());
        assertEquals("REMEDIATE", r.path().getFirst().action());
    }

    @Test void highMasteryCreatesAccelerationPath() {
        var lesson = org.mockito.Mockito.mock(com.masterlearning.platform.modules.course.entity.Lesson.class);
        UUID id = UUID.randomUUID();
        org.mockito.Mockito.when(lesson.getId()).thenReturn(id);
        org.mockito.Mockito.when(lesson.getTitle()).thenReturn("Lesson 1");
        var r = engine.build(UUID.randomUUID(), List.of(lesson), List.of(), Set.of(), 90);
        assertEquals("ACCELERATED", r.learnerState());
        assertEquals("ACCELERATE", r.path().getFirst().action());
    }
}
