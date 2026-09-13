package com.masterlearning.platform.modules.ai.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.ai.dto.response.AdaptiveAssessmentOrchestration;
import com.masterlearning.platform.modules.ai.service.AdaptiveAssessmentOrchestrationService;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning/enrollments/{enrollmentId}/ai")
public class AdaptiveAssessmentOrchestrationController {
    private final AdaptiveAssessmentOrchestrationService service;

    public AdaptiveAssessmentOrchestrationController(AdaptiveAssessmentOrchestrationService service) {
        this.service = service;
    }

    @GetMapping("/assessment-orchestration")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AdaptiveAssessmentOrchestration> decide(@PathVariable UUID enrollmentId) {
        return ApiResponse.success("Adaptive assessment orchestration generated",
                service.decide(enrollmentId, SecurityUtils.getCurrentUserId()));
    }
}
