package com.masterlearning.platform.modules.course.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.assessment.entity.AssessmentAttempt;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAttemptRepository;
import com.masterlearning.platform.modules.course.dto.response.LearningAnalyticsResponse;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.CourseModuleRepository;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.modules.course.repository.LearningActivityRepository;
import com.masterlearning.platform.modules.course.repository.LessonProgressRepository;
import com.masterlearning.platform.modules.course.repository.LessonRepository;
import com.masterlearning.platform.modules.course.service.LearningAnalyticsEngine;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning")
public class LearningAnalyticsController {

    private final EnrollmentRepository enrollments;
    private final CourseModuleRepository modules;
    private final LessonRepository lessons;
    private final LessonProgressRepository progress;
    private final LearningActivityRepository activities;
    private final AssessmentAttemptRepository attempts;
    private final LearningAnalyticsEngine engine;

    public LearningAnalyticsController(EnrollmentRepository enrollments,
                                        CourseModuleRepository modules,
                                        LessonRepository lessons,
                                        LessonProgressRepository progress,
                                        LearningActivityRepository activities,
                                        AssessmentAttemptRepository attempts,
                                        LearningAnalyticsEngine engine) {
        this.enrollments = enrollments;
        this.modules = modules;
        this.lessons = lessons;
        this.progress = progress;
        this.activities = activities;
        this.attempts = attempts;
        this.engine = engine;
    }

    @GetMapping("/enrollments/{enrollmentId}/analytics")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ApiResponse<LearningAnalyticsResponse> analytics(@PathVariable UUID enrollmentId) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        UUID userId = SecurityUtils.getCurrentUserId();
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You cannot access another user's learning analytics");
        }

        UUID courseId = enrollment.getCourse().getId();
        int totalLessons = modules.findByCourseIdOrderBySortOrderAsc(courseId).stream()
                .flatMap(module -> lessons.findByModuleIdOrderBySortOrderAsc(module.getId()).stream())
                .toList().size();
        int completedLessons = (int) progress.countByEnrollmentIdAndCompletedTrue(enrollmentId);

        List<AssessmentAttempt> evidence = attempts.findByCourseIdAndUserIdOrderBySubmittedAtDesc(courseId, userId);
        double masteryScore = evidence.isEmpty() ? 0.0 : evidence.stream()
                .mapToInt(AssessmentAttempt::getScore)
                .average().orElse(0.0);

        LearningAnalyticsResponse result = engine.analyze(
                enrollmentId,
                completedLessons,
                totalLessons,
                masteryScore,
                activities.totalDurationSeconds(enrollmentId),
                enrollment.getCreatedAt(),
                activities.lastActivityAt(enrollmentId));

        return ApiResponse.success("Learning analytics and completion prediction retrieved", result);
    }
}
