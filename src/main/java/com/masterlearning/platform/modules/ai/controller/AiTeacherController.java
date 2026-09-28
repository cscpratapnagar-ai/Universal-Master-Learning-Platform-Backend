package com.masterlearning.platform.modules.ai.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.ai.dto.request.AiTeacherTurnRequest;
import com.masterlearning.platform.modules.ai.dto.response.AiTeacherTurnResponse;
import com.masterlearning.platform.modules.ai.engine.AiTeacherEngine;
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
@RequestMapping("/api/v1/student/learning/enrollments/{enrollmentId}/ai-teacher")
public class AiTeacherController {

    private final EnrollmentRepository enrollments;
    private final AiTeacherEngine engine;

    public AiTeacherController(EnrollmentRepository enrollments, AiTeacherEngine engine) {
        this.enrollments = enrollments;
        this.engine = engine;
    }

    @PostMapping("/turn")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ApiResponse<AiTeacherTurnResponse> turn(
            @PathVariable UUID enrollmentId,
            @RequestBody AiTeacherTurnRequest request) {

        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));

        UUID userId = SecurityUtils.getCurrentUserId();
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You cannot access another user's learning data");
        }

        return ApiResponse.success("AI teacher turn generated", engine.teach(request));
    }
}
