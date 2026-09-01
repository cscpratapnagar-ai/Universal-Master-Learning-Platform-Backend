package com.masterlearning.platform.modules.course.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.course.dto.response.*;
import com.masterlearning.platform.modules.course.entity.LessonProgress;
import com.masterlearning.platform.modules.course.repository.*;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAttemptRepository;
import com.masterlearning.platform.modules.assessment.repository.AssessmentRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning")
public class StudentLearningController {

    private final EnrollmentRepository enrollments;
    private final CourseModuleRepository modules;
    private final LessonRepository lessons;
    private final LessonProgressRepository progress;
    private final AssessmentRepository assessments;
    private final AssessmentAttemptRepository assessmentAttempts;

    public StudentLearningController(EnrollmentRepository e, CourseModuleRepository m,
                                     LessonRepository l, LessonProgressRepository p,
                                     AssessmentRepository assessments,
                                     AssessmentAttemptRepository assessmentAttempts) {
        enrollments = e;
        modules = m;
        lessons = l;
        progress = p;
        this.assessments = assessments;
        this.assessmentAttempts = assessmentAttempts;
    }

    @GetMapping("/enrollments/{enrollmentId}")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ApiResponse<CourseLearningResponse> course(@PathVariable UUID enrollmentId) {
        var e = getOwnedEnrollment(enrollmentId);

        var list = modules.findByCourseIdOrderBySortOrderAsc(e.getCourse().getId()).stream()
                .map(m -> new ModuleResponse(
                        m.getId(),
                        m.getTitle(),
                        m.getSortOrder(),
                        lessons.findByModuleIdOrderBySortOrderAsc(m.getId()).stream()
                                .map(l -> {
                                    boolean done = progress.findByEnrollmentIdAndLessonId(
                                                    enrollmentId, l.getId())
                                            .map(LessonProgress::isCompleted)
                                            .orElse(false);

                                    if (!done && "AUTO_COMPLETE".equals(l.getCompletionMode())) {
                                        markLessonCompleted(e, l, enrollmentId, l.getId());
                                        done = true;
                                    }

                                    return new LessonResponse(
                                            l.getId(), l.getTitle(), l.getContentType(),
                                            l.getContent(), l.getSortOrder(), done
                                    );
                                }).toList()
                )).toList();

        return ApiResponse.success("Course learning content retrieved",
                new CourseLearningResponse(
                        e.getCourse().getId(),
                        e.getCourse().getTitle(),
                        e.getCourse().getDescription(),
                        e.getProgressPercent(),
                        list
                ));
    }

    @PostMapping("/enrollments/{enrollmentId}/lessons/{lessonId}/complete")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ApiResponse<EnrollmentResponse> complete(@PathVariable UUID enrollmentId,
                                                     @PathVariable UUID lessonId) {
        var e = getOwnedEnrollment(enrollmentId);
        var l = lessons.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found"));

        if (!l.getModule().getCourse().getId().equals(e.getCourse().getId())) {
            throw new IllegalArgumentException(
                    "Lesson does not belong to the enrolled course"
            );
        }

        if ("AUTO_COMPLETE".equals(l.getCompletionMode())) {
            return markLessonCompleted(e, l, enrollmentId, lessonId);
        }

        if ("ASSESSMENT_REQUIRED".equals(l.getCompletionMode())) {
            var lessonAssessments = assessments.findByLessonId(lessonId);
            if (lessonAssessments.isEmpty()) {
                throw new IllegalStateException(
                        "Lesson requires an assessment, but no lesson assessment is configured"
                );
            }

            UUID currentUserId = SecurityUtils.getCurrentUserId();
            boolean passed = lessonAssessments.stream().anyMatch(assessment ->
                    assessmentAttempts
                            .findTopByAssessmentIdAndUserIdAndPassedTrueOrderBySubmittedAtDesc(
                                    assessment.getId(), currentUserId)
                            .isPresent()
            );

            if (!passed) {
                throw new IllegalStateException(
                        "You must pass the required lesson assessment before completing this lesson"
                );
            }
        }

        return markLessonCompleted(e, l, enrollmentId, lessonId);
    }

    private ApiResponse<EnrollmentResponse> markLessonCompleted(
            com.masterlearning.platform.modules.course.entity.Enrollment enrollment,
            com.masterlearning.platform.modules.course.entity.Lesson lesson,
            UUID enrollmentId,
            UUID lessonId
    ) {
        var lp = progress.findByEnrollmentIdAndLessonId(enrollmentId, lessonId)
                .orElseGet(() -> progress.save(new LessonProgress(enrollment, lesson)));

        lp.complete();

        long completed = progress.countByEnrollmentIdAndCompletedTrue(enrollmentId);
        long total = modules.findByCourseIdOrderBySortOrderAsc(enrollment.getCourse().getId()).stream()
                .mapToLong(m -> lessons.findByModuleIdOrderBySortOrderAsc(m.getId()).size())
                .sum();

        int percent = total == 0 ? 0 : (int) Math.round(completed * 100.0 / total);
        enrollment.updateProgress(percent);
        enrollments.save(enrollment);

        return ApiResponse.success(
                "Lesson completed and progress updated",
                new EnrollmentResponse(enrollment.getId(), enrollment.getProgressPercent())
        );
    }

    private com.masterlearning.platform.modules.course.entity.Enrollment getOwnedEnrollment(
            UUID enrollmentId
    ) {
        var enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));

        if (!enrollment.getUser().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new AccessDeniedException("You cannot access another user's learning data");
        }

        return enrollment;
    }
}
