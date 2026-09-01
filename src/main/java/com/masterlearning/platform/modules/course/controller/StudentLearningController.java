package com.masterlearning.platform.modules.course.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAttemptRepository;
import com.masterlearning.platform.modules.assessment.repository.AssessmentRepository;
import com.masterlearning.platform.modules.course.dto.response.*;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.entity.Lesson;
import com.masterlearning.platform.modules.course.entity.LessonProgress;
import com.masterlearning.platform.modules.course.repository.*;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
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
    private final LessonPrerequisiteRepository prerequisites;

    public StudentLearningController(EnrollmentRepository e, CourseModuleRepository m,
                                     LessonRepository l, LessonProgressRepository p,
                                     AssessmentRepository assessments,
                                     AssessmentAttemptRepository assessmentAttempts,
                                     LessonPrerequisiteRepository prerequisites) {
        enrollments = e;
        modules = m;
        lessons = l;
        progress = p;
        this.assessments = assessments;
        this.assessmentAttempts = assessmentAttempts;
        this.prerequisites = prerequisites;
    }

    @GetMapping("/enrollments/{enrollmentId}")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ApiResponse<CourseLearningResponse> course(@PathVariable UUID enrollmentId) {
        var e = getOwnedEnrollment(enrollmentId);

        var list = modules.findByCourseIdOrderBySortOrderAsc(e.getCourse().getId()).stream()
                .map(m -> new ModuleResponse(
                        m.getId(),
                        m.getTitle(),
                        m.getSortOrder(),
                        lessons.findByModuleIdOrderBySortOrderAsc(m.getId()).stream()
                                .map(l -> toLessonResponse(e, enrollmentId, l))
                                .toList()
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

    @GetMapping("/enrollments/{enrollmentId}/lessons/{lessonId}/access")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> lessonAccess(
            @PathVariable UUID enrollmentId,
            @PathVariable UUID lessonId
    ) {
        var e = getOwnedEnrollment(enrollmentId);
        var lesson = getCourseLesson(e, lessonId);
        var access = getLessonAccess(enrollmentId, lessonId);

        List<Map<String, Object>> pendingPrerequisites =
                access.unmetPrerequisiteLessonIds().stream()
                        .map(id -> {
                            var prerequisite = lessons.findById(id)
                                    .orElseThrow(() -> new EntityNotFoundException("Prerequisite lesson not found"));
                            return Map.<String, Object>of(
                                    "id", prerequisite.getId(),
                                    "title", prerequisite.getTitle(),
                                    "reason", "INCOMPLETE"
                            );
                        })
                        .toList();

        Map<String, Object> status = Map.of(
                "lessonId", lesson.getId(),
                "locked", !access.unmetPrerequisiteLessonIds().isEmpty(),
                "totalPrerequisites", access.prerequisiteLessonIds().size(),
                "completedPrerequisites",
                access.prerequisiteLessonIds().size() - access.unmetPrerequisiteLessonIds().size(),
                "pendingPrerequisites", pendingPrerequisites,
                "unlockStatus", access.unmetPrerequisiteLessonIds().isEmpty()
                        ? "UNLOCKED"
                        : "COMPLETE_ALL_PENDING_PREREQUISITES"
        );

        return ApiResponse.success("Lesson access status retrieved", status);
    }

    @GetMapping("/enrollments/{enrollmentId}/learning-path")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ApiResponse<Map<String, Object>> learningPath(@PathVariable UUID enrollmentId) {
        var enrollment = getOwnedEnrollment(enrollmentId);

        List<Lesson> courseLessons = modules
                .findByCourseIdOrderBySortOrderAsc(enrollment.getCourse().getId())
                .stream()
                .flatMap(module -> lessons.findByModuleIdOrderBySortOrderAsc(module.getId()).stream())
                .toList();

        List<Map<String, Object>> lessonStatuses = courseLessons.stream()
                .map(lesson -> {
                    boolean completed = progress
                            .findByEnrollmentIdAndLessonId(enrollmentId, lesson.getId())
                            .map(LessonProgress::isCompleted)
                            .orElse(false);
                    var access = getLessonAccess(enrollmentId, lesson.getId());
                    boolean locked = !completed && !access.unmetPrerequisiteLessonIds().isEmpty();

                    return Map.<String, Object>of(
                            "lessonId", lesson.getId(),
                            "title", lesson.getTitle(),
                            "sortOrder", lesson.getSortOrder(),
                            "completed", completed,
                            "locked", locked,
                            "status", completed ? "COMPLETED" : (locked ? "LOCKED" : "AVAILABLE"),
                            "pendingPrerequisiteCount", access.unmetPrerequisiteLessonIds().size()
                    );
                })
                .toList();

        long completedCount = lessonStatuses.stream()
                .filter(status -> "COMPLETED".equals(status.get("status")))
                .count();
        long availableCount = lessonStatuses.stream()
                .filter(status -> "AVAILABLE".equals(status.get("status")))
                .count();
        long lockedCount = lessonStatuses.stream()
                .filter(status -> "LOCKED".equals(status.get("status")))
                .count();

        int recalculatedProgress = courseLessons.isEmpty()
                ? 0
                : (int) Math.round(completedCount * 100.0 / courseLessons.size());

        boolean courseCompleted = !courseLessons.isEmpty()
                && completedCount == courseLessons.size();

        if (enrollment.getProgressPercent() != recalculatedProgress) {
            enrollment.updateProgress(recalculatedProgress);
            enrollments.save(enrollment);
        }

        Map<String, Object> nextLesson = courseCompleted
                ? null
                : lessonStatuses.stream()
                        .filter(status -> "AVAILABLE".equals(status.get("status")))
                        .findFirst()
                        .orElse(null);

        Map<String, Object> learningPath = new LinkedHashMap<>();
        learningPath.put("enrollmentId", enrollmentId);
        learningPath.put("courseId", enrollment.getCourse().getId());
        learningPath.put("progressPercent", recalculatedProgress);
        learningPath.put("completedLessonsCount", completedCount);
        learningPath.put("availableLessonsCount", availableCount);
        learningPath.put("lockedLessonsCount", lockedCount);
        learningPath.put("totalLessonsCount", courseLessons.size());
        learningPath.put("isCourseCompleted", courseCompleted);
        learningPath.put("courseCompletedAt", enrollment.getCompletedAt());
        learningPath.put("nextRecommendedLesson", nextLesson == null ? Map.of() : nextLesson);
        learningPath.put("lessons", lessonStatuses);

        return ApiResponse.success("Learning path retrieved", learningPath);
    }

    @PostMapping("/enrollments/{enrollmentId}/lessons/{lessonId}/complete")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ApiResponse<EnrollmentResponse> complete(@PathVariable UUID enrollmentId,
                                                     @PathVariable UUID lessonId) {
        var e = getOwnedEnrollment(enrollmentId);
        var l = getCourseLesson(e, lessonId);

        var access = getLessonAccess(enrollmentId, lessonId);
        if (!access.unmetPrerequisiteLessonIds().isEmpty()) {
            throw new IllegalStateException(
                    "Lesson is locked. Complete all prerequisite lessons first: "
                            + access.unmetPrerequisiteLessonIds()
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

    private LessonResponse toLessonResponse(Enrollment enrollment, UUID enrollmentId, Lesson lesson) {
        boolean done = progress.findByEnrollmentIdAndLessonId(enrollmentId, lesson.getId())
                .map(LessonProgress::isCompleted)
                .orElse(false);

        var access = getLessonAccess(enrollmentId, lesson.getId());

        if (!done && access.unmetPrerequisiteLessonIds().isEmpty()
                && "AUTO_COMPLETE".equals(lesson.getCompletionMode())) {
            markLessonCompleted(enrollment, lesson, enrollmentId, lesson.getId());
            done = true;
        }

        boolean locked = !done && !access.unmetPrerequisiteLessonIds().isEmpty();

        return new LessonResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getContentType(),
                locked ? null : lesson.getContent(),
                lesson.getSortOrder(),
                done,
                locked,
                access.unmetPrerequisiteLessonIds()
        );
    }

    private LessonAccess getLessonAccess(UUID enrollmentId, UUID lessonId) {
        List<UUID> prerequisiteIds = prerequisites.findByIdLessonId(lessonId).stream()
                .map(p -> p.getPrerequisiteLessonId())
                .toList();

        List<UUID> unmetIds = prerequisiteIds.stream()
                .filter(prerequisiteId -> progress
                        .findByEnrollmentIdAndLessonId(enrollmentId, prerequisiteId)
                        .map(LessonProgress::isCompleted)
                        .orElse(false) == false)
                .toList();

        return new LessonAccess(prerequisiteIds, unmetIds);
    }

    private record LessonAccess(
            List<UUID> prerequisiteLessonIds,
            List<UUID> unmetPrerequisiteLessonIds
    ) {}

    private Enrollment getOwnedEnrollment(UUID enrollmentId) {
        var enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));

        if (!enrollment.getUser().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new AccessDeniedException("You cannot access another user's learning data");
        }

        return enrollment;
    }

    private Lesson getCourseLesson(Enrollment enrollment, UUID lessonId) {
        var lesson = lessons.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found"));

        if (!lesson.getModule().getCourse().getId().equals(enrollment.getCourse().getId())) {
            throw new IllegalArgumentException(
                    "Lesson does not belong to the enrolled course"
            );
        }

        return lesson;
    }

    private ApiResponse<EnrollmentResponse> markLessonCompleted(
            Enrollment enrollment,
            Lesson lesson,
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
}
