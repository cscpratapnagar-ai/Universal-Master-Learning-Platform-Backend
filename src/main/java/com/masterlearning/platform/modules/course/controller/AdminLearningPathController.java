package com.masterlearning.platform.modules.course.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.course.entity.Lesson;
import com.masterlearning.platform.modules.course.entity.LessonPrerequisite;
import com.masterlearning.platform.modules.course.repository.LessonPrerequisiteRepository;
import com.masterlearning.platform.modules.course.repository.LessonRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/learning-path")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR')")
public class AdminLearningPathController {
    private final LessonRepository lessons;
    private final LessonPrerequisiteRepository prerequisites;

    public AdminLearningPathController(LessonRepository lessons, LessonPrerequisiteRepository prerequisites) {
        this.lessons = lessons;
        this.prerequisites = prerequisites;
    }

    @GetMapping("/lessons/{lessonId}/prerequisites")
    @Transactional(readOnly = true)
    public ApiResponse<List<Map<String, Object>>> getPrerequisites(@PathVariable UUID lessonId) {
        Lesson lesson = getLesson(lessonId);
        List<Map<String, Object>> data = prerequisites.findByIdLessonId(lessonId).stream()
                .map(LessonPrerequisite::getPrerequisiteLessonId)
                .map(this::lessonView)
                .toList();
        return ApiResponse.success("Lesson prerequisites retrieved", data);
    }

    @PostMapping("/lessons/{lessonId}/prerequisites/{prerequisiteLessonId}")
    @Transactional
    public ApiResponse<Map<String, Object>> addPrerequisite(
            @PathVariable UUID lessonId,
            @PathVariable UUID prerequisiteLessonId
    ) {
        Lesson lesson = getLesson(lessonId);
        Lesson prerequisite = getLesson(prerequisiteLessonId);

        if (lessonId.equals(prerequisiteLessonId)) {
            throw new IllegalArgumentException("A lesson cannot be its own prerequisite");
        }

        UUID courseId = lesson.getModule().getCourse().getId();
        if (!courseId.equals(prerequisite.getModule().getCourse().getId())) {
            throw new IllegalArgumentException("Prerequisite lesson must belong to the same course");
        }

        if (wouldCreateCycle(lessonId, prerequisiteLessonId)) {
            throw new IllegalArgumentException("This prerequisite would create a learning-path cycle");
        }

        if (!prerequisites.existsByIdLessonIdAndIdPrerequisiteLessonId(lessonId, prerequisiteLessonId)) {
            prerequisites.save(new LessonPrerequisite(lessonId, prerequisiteLessonId));
        }

        return ApiResponse.success("Prerequisite added", lessonView(prerequisiteLessonId));
    }

    @DeleteMapping("/lessons/{lessonId}/prerequisites/{prerequisiteLessonId}")
    @Transactional
    public ApiResponse<Map<String, Object>> removePrerequisite(
            @PathVariable UUID lessonId,
            @PathVariable UUID prerequisiteLessonId
    ) {
        getLesson(lessonId);
        getLesson(prerequisiteLessonId);
        prerequisites.deleteByIdLessonIdAndIdPrerequisiteLessonId(lessonId, prerequisiteLessonId);
        return ApiResponse.success("Prerequisite removed", Map.of(
                "lessonId", lessonId,
                "prerequisiteLessonId", prerequisiteLessonId
        ));
    }

    private Lesson getLesson(UUID lessonId) {
        return lessons.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
    }

    private Map<String, Object> lessonView(UUID lessonId) {
        Lesson lesson = getLesson(lessonId);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", lesson.getId());
        view.put("title", lesson.getTitle());
        view.put("sortOrder", lesson.getSortOrder());
        view.put("moduleId", lesson.getModule().getId());
        view.put("courseId", lesson.getModule().getCourse().getId());
        return view;
    }

    private boolean wouldCreateCycle(UUID lessonId, UUID prerequisiteLessonId) {
        ArrayDeque<UUID> stack = new ArrayDeque<>();
        HashSet<UUID> visited = new HashSet<>();
        stack.push(prerequisiteLessonId);

        while (!stack.isEmpty()) {
            UUID current = stack.pop();
            if (!visited.add(current)) {
                continue;
            }
            if (lessonId.equals(current)) {
                return true;
            }
            prerequisites.findByIdLessonId(current).stream()
                    .map(LessonPrerequisite::getPrerequisiteLessonId)
                    .forEach(stack::push);
        }
        return false;
    }
}
