package com.masterlearning.platform.modules.course.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAttemptRepository;
import com.masterlearning.platform.modules.assessment.repository.AssessmentRepository;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.CourseModuleRepository;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.modules.course.repository.LearningActivityRepository;
import com.masterlearning.platform.modules.course.repository.LessonProgressRepository;
import com.masterlearning.platform.modules.course.repository.LessonRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning")
public class StudentLearningProgressController {

    private final EnrollmentRepository enrollments;
    private final CourseModuleRepository modules;
    private final LessonRepository lessons;
    private final LessonProgressRepository progress;
    private final LearningActivityRepository activities;
    private final AssessmentRepository assessments;
    private final AssessmentAttemptRepository attempts;

    public StudentLearningProgressController(
            EnrollmentRepository enrollments,
            CourseModuleRepository modules,
            LessonRepository lessons,
            LessonProgressRepository progress,
            LearningActivityRepository activities,
            AssessmentRepository assessments,
            AssessmentAttemptRepository attempts) {
        this.enrollments = enrollments;
        this.modules = modules;
        this.lessons = lessons;
        this.progress = progress;
        this.activities = activities;
        this.assessments = assessments;
        this.attempts = attempts;
    }

    @GetMapping("/enrollments/{enrollmentId}/progress")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> progress(@PathVariable UUID enrollmentId) {
        Enrollment enrollment = ownedEnrollment(enrollmentId);
        UUID courseId = enrollment.getCourse().getId();
        UUID userId = SecurityUtils.getCurrentUserId();

        List<UUID> lessonIds = modules.findByCourseIdOrderBySortOrderAsc(courseId).stream()
                .flatMap(module -> lessons.findByModuleIdOrderBySortOrderAsc(module.getId()).stream())
                .map(lesson -> lesson.getId())
                .toList();

        long totalLessons = lessonIds.size();
        long completedLessons = progress.countByEnrollmentIdAndCompletedTrue(enrollmentId);
        int completionPercent = totalLessons == 0
                ? 0
                : (int) Math.round(completedLessons * 100.0 / totalLessons);

        long learningSeconds = activities.totalDurationSeconds(enrollmentId);
        long activeLessons = activities.activeLessonCount(enrollmentId);

        var courseAssessments = assessments.findByCourseId(courseId);
        List<Map<String, Object>> assessmentResults = courseAssessments.stream()
                .map(assessment -> attempts
                        .findTopByAssessmentIdAndUserIdAndPassedTrueOrderBySubmittedAtDesc(assessment.getId(), userId)
                        .map(attempt -> {
                            Map<String, Object> result = new LinkedHashMap<>();
                            result.put("assessmentId", assessment.getId());
                            result.put("lessonId", assessment.getLesson() == null ? null : assessment.getLesson().getId());
                            result.put("title", assessment.getTitle());
                            result.put("score", attempt.getScore());
                            result.put("masteryLevel", attempt.getMasteryLevel());
                            result.put("passed", true);
                            result.put("submittedAt", attempt.getSubmittedAt());
                            return result;
                        })
                        .orElse(null))
                .filter(result -> result != null)
                .toList();

        double masteryScore = assessmentResults.isEmpty()
                ? 0.0
                : assessmentResults.stream()
                .mapToInt(result -> (Integer) result.get("score"))
                .average()
                .orElse(0.0);

        String masteryLevel = masteryLevel(masteryScore);
        String momentum = completionPercent >= 80 && masteryScore >= 80
                ? "EXCELLENT"
                : completionPercent >= 50 || masteryScore >= 70
                ? "ON_TRACK"
                : completedLessons > 0
                ? "BUILDING"
                : "STARTING";

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("enrollmentId", enrollmentId);
        data.put("courseId", courseId);
        data.put("courseTitle", enrollment.getCourse().getTitle());
        data.put("completionPercent", completionPercent);
        data.put("completedLessons", completedLessons);
        data.put("totalLessons", totalLessons);
        data.put("remainingLessons", Math.max(0, totalLessons - completedLessons));
        data.put("activeLessons", activeLessons);
        data.put("learningSeconds", learningSeconds);
        data.put("learningMinutes", Math.round(learningSeconds / 60.0));
        data.put("masteryScore", Math.round(masteryScore * 10.0) / 10.0);
        data.put("masteryLevel", masteryLevel);
        data.put("momentum", momentum);
        data.put("assessmentCount", assessmentResults.size());
        data.put("assessments", assessmentResults);

        return ApiResponse.success("Learning progress and mastery retrieved", data);
    }

    private String masteryLevel(double score) {
        if (score >= 85) return "MASTERED";
        if (score >= 70) return "PROFICIENT";
        if (score >= 50) return "DEVELOPING";
        if (score > 0) return "EMERGING";
        return "NOT_ASSESSED";
    }

    private Enrollment ownedEnrollment(UUID enrollmentId) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));

        if (!enrollment.getUser().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new AccessDeniedException("You cannot access another user's learning progress");
        }

        return enrollment;
    }
}
