package com.masterlearning.platform.modules.ai.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.ai.dto.response.AiKnowledgeContext;
import com.masterlearning.platform.modules.ai.service.AiKnowledgeContextService;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning/enrollments/{enrollmentId}/ai")
public class AiKnowledgeContextController {
    private final AiKnowledgeContextService service;

    public AiKnowledgeContextController(AiKnowledgeContextService service) {
        this.service = service;
    }

    @GetMapping("/knowledge-context")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AiKnowledgeContext> getContext(@PathVariable UUID enrollmentId) {
        return ApiResponse.success("AI knowledge context generated",
                service.forEnrollment(enrollmentId, SecurityUtils.getCurrentUserId()));
    }
}
