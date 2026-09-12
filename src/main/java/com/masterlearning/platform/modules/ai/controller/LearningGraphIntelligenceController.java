package com.masterlearning.platform.modules.ai.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.ai.dto.response.LearningGraphIntelligence;
import com.masterlearning.platform.modules.ai.service.LearningGraphIntelligenceService;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning/enrollments/{enrollmentId}/knowledge-graph")
public class LearningGraphIntelligenceController {
    private final LearningGraphIntelligenceService service;

    public LearningGraphIntelligenceController(LearningGraphIntelligenceService service) {
        this.service = service;
    }

    @GetMapping("/intelligence")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<LearningGraphIntelligence> getIntelligence(@PathVariable UUID enrollmentId) {
        return ApiResponse.success("Learning graph intelligence retrieved",
                service.forEnrollment(enrollmentId, SecurityUtils.getCurrentUserId()));
    }
}
