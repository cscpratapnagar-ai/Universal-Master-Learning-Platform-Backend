package com.masterlearning.platform.modules.assessment.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.assessment.dto.response.AdaptiveAssessmentDecision;
import com.masterlearning.platform.modules.assessment.entity.Assessment;
import com.masterlearning.platform.modules.assessment.repository.AssessmentRepository;
import com.masterlearning.platform.modules.assessment.service.AdaptiveQuestionSelectionService;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning/assessments")
public class AdaptiveAssessmentController {
    private final AssessmentRepository assessments;
    private final AdaptiveQuestionSelectionService selector;
    private final EnrollmentRepository enrollments;

    public AdaptiveAssessmentController(AssessmentRepository assessments,
                                        AdaptiveQuestionSelectionService selector,
                                        EnrollmentRepository enrollments) {
        this.assessments = assessments;
        this.selector = selector;
        this.enrollments = enrollments;
    }

    @GetMapping("/{assessmentId}/adaptive-next")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AdaptiveAssessmentDecision> next(@PathVariable UUID assessmentId) {
        Assessment assessment = assessments.findById(assessmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assessment not found"));
        UUID userId = SecurityUtils.getCurrentUserId();
        if (!enrollments.existsByCourseIdAndUserId(assessment.getCourse().getId(), userId)) {
            throw new org.springframework.security.access.AccessDeniedException("You are not enrolled in this course");
        }
        return ApiResponse.success("Adaptive next question selected",
                selector.select(assessment, SecurityUtils.getCurrentUserId()));
    }
}
