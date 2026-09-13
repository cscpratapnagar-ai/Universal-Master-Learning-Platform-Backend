package com.masterlearning.platform.modules.ai.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.ai.dto.response.AiLearningOrchestration;
import com.masterlearning.platform.modules.ai.service.AiLearningOrchestratorService;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning/enrollments/{enrollmentId}/ai")
public class AiLearningOrchestratorController {
    private final AiLearningOrchestratorService service;

    public AiLearningOrchestratorController(AiLearningOrchestratorService service) {
        this.service = service;
    }

    @GetMapping("/orchestration")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AiLearningOrchestration> orchestrate(@PathVariable UUID enrollmentId) {
        return ApiResponse.success("AI learning orchestration generated",
                service.forEnrollment(enrollmentId, SecurityUtils.getCurrentUserId()));
    }
}
