package com.masterlearning.platform.modules.ai.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.ai.dto.response.GroundedTutorResponse;
import com.masterlearning.platform.modules.ai.engine.GroundedTutorEngine;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning/enrollments/{enrollmentId}/ai-tutor")
public class GroundedTutorController {
    private final EnrollmentRepository enrollments;
    private final GroundedTutorEngine engine;

    public GroundedTutorController(EnrollmentRepository enrollments, GroundedTutorEngine engine) {
        this.enrollments = enrollments;
        this.engine = engine;
    }

    @PostMapping("/grounded-respond")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<GroundedTutorResponse> respond(
            @PathVariable UUID enrollmentId,
            @RequestBody TutorRequest request) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        UUID userId = SecurityUtils.getCurrentUserId();
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You cannot access another user's learning data");
        }
        return ApiResponse.success("Grounded AI tutor response generated",
                engine.respond(enrollmentId, enrollment.getCourse().getId(), request == null ? null : request.message()));
    }

    public record TutorRequest(String message) {}
}
