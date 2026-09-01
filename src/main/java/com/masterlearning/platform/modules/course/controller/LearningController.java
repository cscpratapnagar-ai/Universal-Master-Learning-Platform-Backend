package com.masterlearning.platform.modules.course.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.course.dto.request.*;
import com.masterlearning.platform.modules.course.dto.response.EnrollmentResponse;
import com.masterlearning.platform.modules.course.entity.*;
import com.masterlearning.platform.modules.course.repository.*;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learning")
public class LearningController {

    private final CourseRepository courses;
    private final CourseModuleRepository modules;
    private final LessonRepository lessons;
    private final EnrollmentRepository enrollments;
    private final UserRepository users;
    private final LessonPrerequisiteRepository prerequisites;

    public LearningController(CourseRepository c, CourseModuleRepository m, LessonRepository l,
                              EnrollmentRepository e, UserRepository u,
                              LessonPrerequisiteRepository prerequisites) {
        courses = c;
        modules = m;
        lessons = l;
        enrollments = e;
        users = u;
        this.prerequisites = prerequisites;
    }

    @PostMapping("/courses/{courseId}/modules")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR')")
    public ApiResponse<Map<String, Object>> addModule(@PathVariable UUID courseId,
                                                       @Valid @RequestBody CreateModuleRequest r) {
        var c = courses.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        var m = modules.save(new CourseModule(c, r.title(), r.sortOrder()));
        return ApiResponse.success("Module created", Map.of("id", m.getId(), "title", m.getTitle()));
    }

    @PostMapping("/modules/{moduleId}/lessons")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR')")
    public ApiResponse<Map<String, Object>> addLesson(@PathVariable UUID moduleId,
                                                       @Valid @RequestBody CreateLessonRequest r) {
        var m = modules.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Module not found"));
        var l = lessons.save(new Lesson(
                m, r.title(), r.contentType() == null ? "TEXT" : r.contentType(),
                r.content(), r.sortOrder()
        ));
        return ApiResponse.success("Lesson created", Map.of("id", l.getId(), "title", l.getTitle()));
    }

    @PatchMapping("/lessons/{lessonId}/completion-mode")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR')")
    public ApiResponse<Map<String, Object>> updateLessonCompletionMode(
            @PathVariable UUID lessonId,
            @RequestBody Map<String, String> body
    ) {
        var lesson = lessons.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found"));

        String mode = body.get("completionMode");
        if (mode == null || !Set.of(
                "AUTO_COMPLETE", "MANUAL_COMPLETE", "ASSESSMENT_REQUIRED"
        ).contains(mode)) {
            throw new IllegalArgumentException("Invalid completion mode");
        }

        lesson.setCompletionMode(mode);
        lessons.save(lesson);
        return ApiResponse.success("Lesson completion mode updated", Map.of(
                "id", lesson.getId(),
                "completionMode", lesson.getCompletionMode()
        ));
    }

    @PostMapping("/lessons/{lessonId}/prerequisites/{prerequisiteLessonId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR')")
    @Transactional
    public ApiResponse<Map<String, Object>> addPrerequisite(
            @PathVariable UUID lessonId,
            @PathVariable UUID prerequisiteLessonId
    ) {
        if (lessonId.equals(prerequisiteLessonId)) {
            throw new IllegalArgumentException("A lesson cannot depend on itself");
        }

        var lesson = lessons.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found"));
        var prerequisite = lessons.findById(prerequisiteLessonId)
                .orElseThrow(() -> new EntityNotFoundException("Prerequisite lesson not found"));

        if (!lesson.getModule().getCourse().getId()
                .equals(prerequisite.getModule().getCourse().getId())) {
            throw new IllegalArgumentException("Prerequisite lesson must belong to the same course");
        }

        if (prerequisites.existsByIdLessonIdAndIdPrerequisiteLessonId(
                lessonId, prerequisiteLessonId)) {
            throw new IllegalArgumentException("Prerequisite already configured");
        }

        if (dependsOn(prerequisiteLessonId, lessonId, new HashSet<>())) {
            throw new IllegalArgumentException(
                    "Prerequisite would create a circular lesson dependency"
            );
        }

        prerequisites.saveAndFlush(new LessonPrerequisite(lessonId, prerequisiteLessonId));
        return ApiResponse.success("Lesson prerequisite added", Map.of(
                "lessonId", lessonId,
                "prerequisiteLessonId", prerequisiteLessonId
        ));
    }

    @DeleteMapping("/lessons/{lessonId}/prerequisites/{prerequisiteLessonId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR')")
    @Transactional
    public ApiResponse<Map<String, Object>> removePrerequisite(
            @PathVariable UUID lessonId,
            @PathVariable UUID prerequisiteLessonId
    ) {
        if (!prerequisites.existsByIdLessonIdAndIdPrerequisiteLessonId(
                lessonId, prerequisiteLessonId)) {
            throw new EntityNotFoundException("Lesson prerequisite not found");
        }

        prerequisites.deleteByIdLessonIdAndIdPrerequisiteLessonId(
                lessonId, prerequisiteLessonId);
        return ApiResponse.success("Lesson prerequisite removed", Map.of(
                "lessonId", lessonId,
                "prerequisiteLessonId", prerequisiteLessonId
        ));
    }

    private boolean dependsOn(UUID lessonId, UUID targetLessonId, Set<UUID> visited) {
        if (!visited.add(lessonId)) {
            return false;
        }

        for (var dependency : prerequisites.findByIdLessonId(lessonId)) {
            UUID prerequisiteId = dependency.getPrerequisiteLessonId();
            if (prerequisiteId.equals(targetLessonId)
                    || dependsOn(prerequisiteId, targetLessonId, visited)) {
                return true;
            }
        }

        return false;
    }

    @GetMapping("/courses/{courseId}/dependency-graph")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR')")
    public ApiResponse<Map<String, Object>> dependencyGraph(@PathVariable UUID courseId) {
        var course = courses.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();

        for (var module : modules.findByCourseIdOrderBySortOrderAsc(course.getId())) {
            for (var lesson : lessons.findByModuleIdOrderBySortOrderAsc(module.getId())) {
                nodes.add(Map.of(
                        "id", lesson.getId(),
                        "title", lesson.getTitle(),
                        "moduleId", module.getId(),
                        "moduleTitle", module.getTitle(),
                        "sortOrder", lesson.getSortOrder()
                ));

                for (var prerequisite : prerequisites.findByIdLessonId(lesson.getId())) {
                    edges.add(Map.of(
                            "from", prerequisite.getPrerequisiteLessonId(),
                            "to", lesson.getId()
                    ));
                }
            }
        }

        return ApiResponse.success("Course dependency graph retrieved", Map.of(
                "courseId", course.getId(),
                "nodes", nodes,
                "edges", edges
        ));
    }

    @PostMapping("/courses/{courseId}/enroll")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<EnrollmentResponse> enroll(@PathVariable UUID courseId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        if (enrollments.existsByCourseIdAndUserId(courseId, currentUserId)) {
            throw new IllegalArgumentException("User already enrolled");
        }

        var c = courses.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        var u = users.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));

        var e = enrollments.save(new Enrollment(c, u));
        return ApiResponse.success(
                "Enrollment successful",
                new EnrollmentResponse(e.getId(), e.getProgressPercent())
        );
    }

    @PutMapping("/enrollments/{id}/progress")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<EnrollmentResponse> progress(@PathVariable UUID id,
                                                     @Valid @RequestBody ProgressRequest r) {
        var e = enrollments.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));

        if (!e.getUser().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You cannot update another user's enrollment"
            );
        }

        e.updateProgress(r.progressPercent());
        enrollments.save(e);

        return ApiResponse.success(
                "Progress updated",
                new EnrollmentResponse(e.getId(), e.getProgressPercent())
        );
    }
}
