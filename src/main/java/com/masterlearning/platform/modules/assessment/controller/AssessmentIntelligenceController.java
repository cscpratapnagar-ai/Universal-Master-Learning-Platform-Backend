package com.masterlearning.platform.modules.assessment.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.assessment.dto.response.AssessmentIntelligenceResponse;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAttemptRepository;
import com.masterlearning.platform.modules.assessment.service.AssessmentIntelligenceEngine;
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
@RequestMapping("/api/v1/student/learning")
public class AssessmentIntelligenceController {
    private final EnrollmentRepository enrollments;
    private final AssessmentAttemptRepository attempts;
    private final AssessmentIntelligenceEngine engine;

    public AssessmentIntelligenceController(EnrollmentRepository enrollments,
                                             AssessmentAttemptRepository attempts,
                                             AssessmentIntelligenceEngine engine) {
        this.enrollments = enrollments;
        this.attempts = attempts;
        this.engine = engine;
    }

    @GetMapping("/enrollments/{enrollmentId}/assessment-intelligence")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ApiResponse<AssessmentIntelligenceResponse> intelligence(@PathVariable UUID enrollmentId) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));
        UUID userId = SecurityUtils.getCurrentUserId();
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You cannot access another user's assessment intelligence");
        }
        return ApiResponse.success("Assessment intelligence generated", engine.analyze(
                enrollmentId,
                attempts.findByCourseIdAndUserIdOrderBySubmittedAtDesc(enrollment.getCourse().getId(), userId)));
    }
}
