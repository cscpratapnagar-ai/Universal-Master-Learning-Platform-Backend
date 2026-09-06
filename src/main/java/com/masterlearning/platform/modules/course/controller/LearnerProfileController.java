package com.masterlearning.platform.modules.course.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.assessment.entity.AssessmentAttempt;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAttemptRepository;
import com.masterlearning.platform.modules.course.dto.response.LearnerProfileResponse;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.CourseModuleRepository;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.modules.course.repository.LessonProgressRepository;
import com.masterlearning.platform.modules.course.repository.LessonRepository;
import com.masterlearning.platform.modules.course.service.LearnerProfileEngine;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning")
public class LearnerProfileController {
    private final EnrollmentRepository enrollments;
    private final CourseModuleRepository modules;
    private final LessonRepository lessons;
    private final LessonProgressRepository progress;
    private final AssessmentAttemptRepository attempts;
    private final LearnerProfileEngine engine;

    public LearnerProfileController(EnrollmentRepository enrollments, CourseModuleRepository modules,
                                    LessonRepository lessons, LessonProgressRepository progress,
                                    AssessmentAttemptRepository attempts, LearnerProfileEngine engine) {
        this.enrollments = enrollments; this.modules = modules; this.lessons = lessons;
        this.progress = progress; this.attempts = attempts; this.engine = engine;
    }

    @GetMapping("/enrollments/{enrollmentId}/profile")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ApiResponse<LearnerProfileResponse> profile(@PathVariable UUID enrollmentId) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        UUID userId = SecurityUtils.getCurrentUserId();
        if (!enrollment.getUser().getId().equals(userId))
            throw new AccessDeniedException("You cannot access another user's learner profile");

        UUID courseId = enrollment.getCourse().getId();
        List<UUID> lessonIds = modules.findByCourseIdOrderBySortOrderAsc(courseId).stream()
                .flatMap(m -> lessons.findByModuleIdOrderBySortOrderAsc(m.getId()).stream())
                .map(l -> l.getId()).toList();
        Set<UUID> completed = new HashSet<>();
        for (UUID lessonId : lessonIds) {
            progress.findByEnrollmentIdAndLessonId(enrollmentId, lessonId)
                    .filter(p -> p.isCompleted()).ifPresent(p -> completed.add(lessonId));
        }
        int completion = lessonIds.isEmpty() ? 0 : (int) Math.round(completed.size() * 100.0 / lessonIds.size());
        List<AssessmentAttempt> evidence = attempts.findByCourseIdAndUserIdOrderBySubmittedAtDesc(courseId, userId);
        double mastery = evidence.stream().mapToInt(AssessmentAttempt::getScore).average().orElse(0);
        long passed = evidence.stream().filter(AssessmentAttempt::isPassed).count();
        boolean declining = evidence.size() >= 2 && evidence.get(0).getScore() <= evidence.get(1).getScore() - 5;
        boolean gaps = evidence.stream().anyMatch(a -> !a.isPassed() || a.getScore() < 50);
        String masteryLevel = mastery >= 90 ? "MASTERED" : mastery >= 70 ? "PROFICIENT" : mastery > 0 ? "DEVELOPING" : "NOT_ASSESSED";
        String momentum = completion >= 80 && mastery >= 80 ? "EXCELLENT" : completion >= 50 || mastery >= 70 ? "ON_TRACK" : completed.isEmpty() ? "STARTING" : "BUILDING";
        return ApiResponse.success("Unified learner profile retrieved", engine.build(enrollmentId, userId, completion,
                completed.size(), mastery, masteryLevel, momentum, evidence.size(), passed, gaps, declining));
    }
}
