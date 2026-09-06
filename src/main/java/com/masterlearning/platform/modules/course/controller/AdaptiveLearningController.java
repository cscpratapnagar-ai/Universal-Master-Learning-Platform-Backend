package com.masterlearning.platform.modules.course.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.assessment.entity.AssessmentAttempt;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAttemptRepository;
import com.masterlearning.platform.modules.assessment.repository.AssessmentRepository;
import com.masterlearning.platform.modules.course.dto.response.AdaptiveNextActionResponse;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.CourseModuleRepository;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.modules.course.repository.LessonPrerequisiteRepository;
import com.masterlearning.platform.modules.course.repository.LessonProgressRepository;
import com.masterlearning.platform.modules.course.repository.LessonRepository;
import com.masterlearning.platform.modules.course.service.AdaptiveDecisionEngine;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/student/learning")
public class AdaptiveLearningController {

    private final EnrollmentRepository enrollments;
    private final CourseModuleRepository modules;
    private final LessonRepository lessons;
    private final LessonProgressRepository progress;
    private final LessonPrerequisiteRepository prerequisites;
    private final AssessmentRepository assessments;
    private final AssessmentAttemptRepository attempts;
    private final AdaptiveDecisionEngine decisionEngine;

    public AdaptiveLearningController(
            EnrollmentRepository enrollments,
            CourseModuleRepository modules,
            LessonRepository lessons,
            LessonProgressRepository progress,
            LessonPrerequisiteRepository prerequisites,
            AssessmentRepository assessments,
            AssessmentAttemptRepository attempts,
            AdaptiveDecisionEngine decisionEngine) {
        this.enrollments = enrollments;
        this.modules = modules;
        this.lessons = lessons;
        this.progress = progress;
        this.prerequisites = prerequisites;
        this.assessments = assessments;
        this.attempts = attempts;
        this.decisionEngine = decisionEngine;
    }

    @GetMapping("/enrollments/{enrollmentId}/next-action")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ApiResponse<AdaptiveNextActionResponse> nextAction(@PathVariable UUID enrollmentId) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));

        UUID userId = SecurityUtils.getCurrentUserId();
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You cannot access another user's adaptive learning plan");
        }

        UUID courseId = enrollment.getCourse().getId();
        List<com.masterlearning.platform.modules.course.entity.Lesson> orderedLessons =
                modules.findByCourseIdOrderBySortOrderAsc(courseId).stream()
                        .flatMap(module -> lessons.findByModuleIdOrderBySortOrderAsc(module.getId()).stream())
                        .toList();

        Set<UUID> completedLessonIds = new HashSet<>();
        for (var lesson : orderedLessons) {
            progress.findByEnrollmentIdAndLessonId(enrollmentId, lesson.getId())
                    .filter(com.masterlearning.platform.modules.course.entity.LessonProgress::isCompleted)
                    .ifPresent(ignored -> completedLessonIds.add(lesson.getId()));
        }

        List<AssessmentAttempt> passedAttempts = attempts
                .findByCourseIdAndUserIdOrderBySubmittedAtDesc(courseId, userId).stream()
                .filter(AssessmentAttempt::isPassed)
                .toList();

        double masteryScore = passedAttempts.stream()
                .mapToInt(AssessmentAttempt::getScore)
                .average()
                .orElse(0.0);

        List<com.masterlearning.platform.modules.course.entity.LessonPrerequisite> links = new ArrayList<>();
        for (var lesson : orderedLessons) {
            links.addAll(prerequisites.findByIdLessonId(lesson.getId()));
        }

        AdaptiveNextActionResponse decision = decisionEngine.decide(
                orderedLessons, links, completedLessonIds, masteryScore);

        return ApiResponse.success("Adaptive next learning action determined", decision);
    }
}
