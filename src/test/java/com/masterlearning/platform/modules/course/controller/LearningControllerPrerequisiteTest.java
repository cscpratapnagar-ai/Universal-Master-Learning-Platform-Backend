package com.masterlearning.platform.modules.course.controller;

import com.masterlearning.platform.modules.course.entity.Course;
import com.masterlearning.platform.modules.course.entity.CourseModule;
import com.masterlearning.platform.modules.course.entity.Lesson;
import com.masterlearning.platform.modules.course.entity.LessonPrerequisite;
import com.masterlearning.platform.modules.course.repository.LessonPrerequisiteRepository;
import com.masterlearning.platform.modules.course.repository.LessonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningControllerPrerequisiteTest {

    @Mock private LessonRepository lessons;
    @Mock private LessonPrerequisiteRepository prerequisites;
    @InjectMocks private LearningController controller;

    @Test
    void rejectsSelfPrerequisite() {
        UUID lessonId = UUID.randomUUID();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.addPrerequisite(lessonId, lessonId));
        assertEquals("A lesson cannot depend on itself", ex.getMessage());
        verifyNoInteractions(lessons, prerequisites);
    }

    @Test
    void rejectsCircularPrerequisite() {
        UUID lessonA = UUID.randomUUID();
        UUID lessonB = UUID.randomUUID();
        Lesson a = lesson("A");
        Lesson b = lesson("B");
        putInSameCourse(a, b);

        when(lessons.findById(lessonA)).thenReturn(Optional.of(a));
        when(lessons.findById(lessonB)).thenReturn(Optional.of(b));
        when(prerequisites.existsByIdLessonIdAndIdPrerequisiteLessonId(lessonA, lessonB)).thenReturn(false);
        when(prerequisites.findByIdLessonId(lessonB))
                .thenReturn(List.of(new LessonPrerequisite(lessonB, lessonA)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.addPrerequisite(lessonA, lessonB));

        assertEquals("Prerequisite would create a circular lesson dependency", ex.getMessage());
        verify(prerequisites, never()).saveAndFlush(any());
    }

    @Test
    void rejectsDeepCircularPrerequisite() {
        UUID aId = UUID.randomUUID();
        UUID bId = UUID.randomUUID();
        UUID cId = UUID.randomUUID();
        UUID dId = UUID.randomUUID();

        Lesson a = lesson("A");
        Lesson d = lesson("D");
        putInSameCourse(a, d);

        when(lessons.findById(aId)).thenReturn(Optional.of(a));
        when(lessons.findById(dId)).thenReturn(Optional.of(d));
        when(prerequisites.existsByIdLessonIdAndIdPrerequisiteLessonId(aId, dId)).thenReturn(false);
        when(prerequisites.findByIdLessonId(dId)).thenReturn(List.of(new LessonPrerequisite(dId, cId)));
        when(prerequisites.findByIdLessonId(cId)).thenReturn(List.of(new LessonPrerequisite(cId, bId)));
        when(prerequisites.findByIdLessonId(bId)).thenReturn(List.of(new LessonPrerequisite(bId, aId)));

        assertThrows(IllegalArgumentException.class, () -> controller.addPrerequisite(aId, dId));
        verify(prerequisites, never()).saveAndFlush(any());
    }

    @Test
    void allowsMultiplePrerequisitesForSameLesson() {
        UUID lessonB = UUID.randomUUID();
        UUID prerequisiteA = UUID.randomUUID();
        UUID prerequisiteC = UUID.randomUUID();
        Lesson b = lesson("B");
        Lesson a = lesson("A");
        Lesson c = lesson("C");
        putInSameCourse(b, a);
        putInSameCourse(b, c);

        when(lessons.findById(lessonB)).thenReturn(Optional.of(b));
        when(lessons.findById(prerequisiteA)).thenReturn(Optional.of(a));
        when(lessons.findById(prerequisiteC)).thenReturn(Optional.of(c));
        when(prerequisites.existsByIdLessonIdAndIdPrerequisiteLessonId(lessonB, prerequisiteA)).thenReturn(false);
        when(prerequisites.existsByIdLessonIdAndIdPrerequisiteLessonId(lessonB, prerequisiteC)).thenReturn(false);
        when(prerequisites.findByIdLessonId(prerequisiteA)).thenReturn(List.of());
        when(prerequisites.findByIdLessonId(prerequisiteC)).thenReturn(List.of());

        controller.addPrerequisite(lessonB, prerequisiteA);
        controller.addPrerequisite(lessonB, prerequisiteC);

        ArgumentCaptor<LessonPrerequisite> captor = ArgumentCaptor.forClass(LessonPrerequisite.class);
        verify(prerequisites, times(2)).saveAndFlush(captor.capture());

        Set<UUID> saved = new HashSet<>();
        for (LessonPrerequisite value : captor.getAllValues()) {
            assertEquals(lessonB, value.getLessonId());
            saved.add(value.getPrerequisiteLessonId());
        }
        assertEquals(Set.of(prerequisiteA, prerequisiteC), saved);
    }

    private Lesson lesson(String title) {
        Course course = new Course("Course", "course-" + UUID.randomUUID(), "Test", null);
        setCourseId(course, UUID.randomUUID());
        return new Lesson(new CourseModule(course, "Module", 1), title, "TEXT", "content", 1);
    }

    private void setCourseId(Course course, UUID id) {
        try {
            Field field = Course.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(course, id);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private void putInSameCourse(Lesson first, Lesson second) {
        try {
            Field module = Lesson.class.getDeclaredField("module");
            module.setAccessible(true);
            module.set(second, new CourseModule(first.getModule().getCourse(), "Module", 1));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
