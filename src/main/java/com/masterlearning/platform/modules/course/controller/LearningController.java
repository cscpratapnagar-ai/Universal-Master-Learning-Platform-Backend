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
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learning")
public class LearningController {

    private final CourseRepository courses;
    private final CourseModuleRepository modules;
    private final LessonRepository lessons;
    private final EnrollmentRepository enrollments;
    private final UserRepository users;

    public LearningController(CourseRepository c, CourseModuleRepository m, LessonRepository l,
                              EnrollmentRepository e, UserRepository u) {
        courses = c;
        modules = m;
        lessons = l;
        enrollments = e;
        users = u;
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
    @PreAuthorize("hasAnyRole(\'SUPER_ADMIN\',\'ADMIN\',\'INSTRUCTOR\')")
    public ApiResponse<Map<String, Object>> updateLessonCompletionMode(
            @PathVariable UUID lessonId,
            @RequestBody Map<String, String> body
    ) {
        var lesson = lessons.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found"));

        String mode = body.get("completionMode");
        if (mode == null || !java.util.Set.of(
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
