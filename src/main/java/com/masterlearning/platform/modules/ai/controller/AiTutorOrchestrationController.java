package com.masterlearning.platform.modules.ai.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.ai.dto.response.AiTutorOrchestration;
import com.masterlearning.platform.modules.ai.service.AiTutorOrchestrationService;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning/enrollments/{enrollmentId}/ai")
public class AiTutorOrchestrationController {
    private final AiTutorOrchestrationService service;

    public AiTutorOrchestrationController(AiTutorOrchestrationService service) {
        this.service = service;
    }

    @PostMapping("/tutor-orchestration")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AiTutorOrchestration> decide(
            @PathVariable UUID enrollmentId,
            @RequestBody TutorQuestion request) {
        String question = request == null ? null : request.question();
        return ApiResponse.success("AI tutor orchestration generated",
                service.decide(enrollmentId, SecurityUtils.getCurrentUserId(), question));
    }

    public record TutorQuestion(String question) {}
}
