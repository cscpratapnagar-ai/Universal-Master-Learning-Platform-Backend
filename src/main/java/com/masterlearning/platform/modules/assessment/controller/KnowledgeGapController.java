package com.masterlearning.platform.modules.assessment.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.assessment.dto.response.KnowledgeGapResponse;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAnswerRepository;
import com.masterlearning.platform.modules.assessment.service.KnowledgeGapEngine;
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
public class KnowledgeGapController {

    private final EnrollmentRepository enrollments;
    private final AssessmentAnswerRepository answers;
    private final KnowledgeGapEngine engine;

    public KnowledgeGapController(EnrollmentRepository enrollments,
                                  AssessmentAnswerRepository answers,
                                  KnowledgeGapEngine engine) {
        this.enrollments = enrollments;
        this.answers = answers;
        this.engine = engine;
    }

    @GetMapping("/enrollments/{enrollmentId}/knowledge-gaps")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ApiResponse<KnowledgeGapResponse> knowledgeGaps(@PathVariable UUID enrollmentId) {
        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));

        UUID userId = SecurityUtils.getCurrentUserId();
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You cannot access another user's knowledge gaps");
        }

        KnowledgeGapResponse result = engine.analyze(
                answers.findLearnerKnowledgeEvidence(enrollment.getCourse().getId(), userId));
        return ApiResponse.success("Learner knowledge gaps analyzed", result);
    }
}
