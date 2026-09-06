package com.masterlearning.platform.modules.course.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.course.entity.LearningActivity;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.modules.course.repository.LearningActivityRepository;
import com.masterlearning.platform.modules.course.repository.LessonRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning")
public class StudentLearningActivityController {

    private static final Set<String> ALLOWED_EVENTS = Set.of(
            "LESSON_OPENED", "LESSON_VIEWED", "LESSON_HEARTBEAT",
            "LESSON_PAUSED", "LESSON_RESUMED", "LESSON_COMPLETED"
    );

    private final EnrollmentRepository enrollments;
    private final LessonRepository lessons;
    private final LearningActivityRepository activities;

    public StudentLearningActivityController(EnrollmentRepository enrollments,
                                             LessonRepository lessons,
                                             LearningActivityRepository activities) {
        this.enrollments = enrollments;
        this.lessons = lessons;
        this.activities = activities;
    }

    @PostMapping("/enrollments/{enrollmentId}/lessons/{lessonId}/activity")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ApiResponse<Map<String, Object>> recordActivity(
            @PathVariable UUID enrollmentId,
            @PathVariable UUID lessonId,
            @RequestBody ActivityRequest request) {

        UUID userId = SecurityUtils.getCurrentUserId();
        var enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));

        if (!enrollment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You cannot record activity for another user's enrollment");
        }

        var lesson = lessons.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found"));

        if (!lesson.getModule().getCourse().getId().equals(enrollment.getCourse().getId())) {
            throw new IllegalArgumentException("Lesson does not belong to the enrolled course");
        }

        String eventType = request.eventType() == null ? "LESSON_VIEWED" : request.eventType().trim().toUpperCase();
        if (!ALLOWED_EVENTS.contains(eventType)) {
            throw new IllegalArgumentException("Unsupported learning activity event type");
        }

        int duration = Math.max(0, Math.min(request.durationSeconds() == null ? 0 : request.durationSeconds(), 300));
        var activity = activities.save(new LearningActivity(enrollment, lesson, userId, eventType, duration));

        return ApiResponse.success("Learning activity recorded", Map.of(
                "activityId", activity.getId(),
                "enrollmentId", enrollmentId,
                "lessonId", lessonId,
                "eventType", eventType,
                "durationSeconds", duration,
                "occurredAt", activity.getOccurredAt()
        ));
    }

    @GetMapping("/enrollments/{enrollmentId}/activity-summary")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> activitySummary(@PathVariable UUID enrollmentId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        var enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));

        if (!enrollment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You cannot access another user's learning activity");
        }

        long totalSeconds = activities.totalDurationSeconds(enrollmentId);
        long activeLessons = activities.activeLessonCount(enrollmentId);
        Instant lastActivityAt = activities.lastActivityAt(enrollmentId);

        List<Map<String, Object>> recent = activities.findTop20ByEnrollmentIdOrderByOccurredAtDesc(enrollmentId)
                .stream()
                .map(activity -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("activityId", activity.getId());
                    item.put("lessonId", activity.getLesson().getId());
                    item.put("lessonTitle", activity.getLesson().getTitle());
                    item.put("eventType", activity.getEventType());
                    item.put("durationSeconds", activity.getDurationSeconds());
                    item.put("occurredAt", activity.getOccurredAt());
                    return item;
                })
                .toList();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("enrollmentId", enrollmentId);
        summary.put("totalLearningSeconds", totalSeconds);
        summary.put("totalLearningMinutes", Math.round(totalSeconds / 60.0));
        summary.put("activeLessonCount", activeLessons);
        summary.put("lastActivityAt", lastActivityAt);
        summary.put("recentActivities", recent);

        return ApiResponse.success("Learning activity summary retrieved", summary);
    }

    public record ActivityRequest(String eventType, Integer durationSeconds) {}
}
