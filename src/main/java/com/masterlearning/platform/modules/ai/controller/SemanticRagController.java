package com.masterlearning.platform.modules.ai.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.ai.service.SemanticRagService;
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
public class SemanticRagController {
    private final EnrollmentRepository enrollments;
    private final SemanticRagService semanticRag;

    public SemanticRagController(EnrollmentRepository enrollments, SemanticRagService semanticRag) {
        this.enrollments = enrollments;
        this.semanticRag = semanticRag;
    }

    @PostMapping("/index-course")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ApiResponse<SemanticRagService.IndexResult> indexCourse(@PathVariable UUID enrollmentId) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        UUID userId = SecurityUtils.getCurrentUserId();
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You cannot access another user's learning data");
        }
        return ApiResponse.success("Course semantic index built",
                semanticRag.indexCourse(enrollment.getCourse().getId()));
    }

    @GetMapping("/index-status")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ApiResponse<Long> indexStatus(@PathVariable UUID enrollmentId) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        UUID userId = SecurityUtils.getCurrentUserId();
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You cannot access another user's learning data");
        }
        return ApiResponse.success("Semantic index status loaded",
                semanticRag.indexedChunkCount(enrollment.getCourse().getId()));
    }
}
