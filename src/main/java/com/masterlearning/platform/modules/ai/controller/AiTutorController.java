package com.masterlearning.platform.modules.ai.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.ai.dto.response.AiTutorResponse;
import com.masterlearning.platform.modules.ai.engine.AiTutorEngine;
import com.masterlearning.platform.modules.assessment.entity.AssessmentAttempt;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAttemptRepository;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning/enrollments/{enrollmentId}/ai-tutor")
public class AiTutorController {
    private final EnrollmentRepository enrollments;
    private final AssessmentAttemptRepository attempts;
    private final AiTutorEngine engine;

    public AiTutorController(EnrollmentRepository enrollments, AssessmentAttemptRepository attempts, AiTutorEngine engine) {
        this.enrollments = enrollments;
        this.attempts = attempts;
        this.engine = engine;
    }

    @PostMapping("/respond")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ApiResponse<AiTutorResponse> respond(@PathVariable UUID enrollmentId,
                                                 @RequestBody TutorRequest request) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        UUID userId = SecurityUtils.getCurrentUserId();
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You cannot access another user's learning data");
        }

        var history = attempts.findByCourseIdAndUserIdOrderBySubmittedAtDesc(
                enrollment.getCourse().getId(), userId);
        double mastery = history.isEmpty() ? 0.0 : history.stream()
                .limit(5)
                .mapToInt(AssessmentAttempt::getScore)
                .average().orElse(0.0);
        int weakAreas = (int) history.stream().limit(5).filter(a -> a.getScore() < 60).count();

        return ApiResponse.success("AI tutor response generated",
                engine.respond(enrollmentId, request == null ? null : request.message(),
                        mastery, enrollment.getProgressPercent(), weakAreas));
    }

    public record TutorRequest(String message) {}
}
