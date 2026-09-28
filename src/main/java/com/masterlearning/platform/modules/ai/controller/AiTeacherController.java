package com.masterlearning.platform.modules.ai.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.ai.dto.request.AiTeacherTurnRequest;
import com.masterlearning.platform.modules.ai.dto.response.AiTeacherQuotaResponse;
import com.masterlearning.platform.modules.ai.dto.response.AiTeacherTurnResponse;
import com.masterlearning.platform.modules.ai.engine.AiTeacherEngine;
import com.masterlearning.platform.modules.ai.service.AiTeacherUsageService;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning/enrollments/{enrollmentId}/ai-teacher")
public class AiTeacherController {

    private final EnrollmentRepository enrollments;
    private final AiTeacherEngine engine;
    private final AiTeacherUsageService usage;

    public AiTeacherController(
            EnrollmentRepository enrollments,
            AiTeacherEngine engine,
            AiTeacherUsageService usage) {
        this.enrollments = enrollments;
        this.engine = engine;
        this.usage = usage;
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

        try {
            usage.consumeTurn(userId);
        } catch (AiTeacherUsageService.AiTeacherQuotaExceededException ex) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "AI Teacher monthly quota reached for the " + ex.getPlan() + " plan");
        }

        return ApiResponse.success("AI teacher turn generated", engine.teach(request));
    }

    @GetMapping("/quota")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AiTeacherQuotaResponse> quota() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success("AI teacher quota loaded", usage.getQuota(userId));
    }
}
