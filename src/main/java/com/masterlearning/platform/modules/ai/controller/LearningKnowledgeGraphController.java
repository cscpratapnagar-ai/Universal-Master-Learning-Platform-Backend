package com.masterlearning.platform.modules.ai.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.ai.dto.response.LearningKnowledgeGraph;
import com.masterlearning.platform.modules.ai.service.LearningKnowledgeGraphService;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning/enrollments/{enrollmentId}/knowledge-graph")
public class LearningKnowledgeGraphController {
    private final LearningKnowledgeGraphService service;

    public LearningKnowledgeGraphController(LearningKnowledgeGraphService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<LearningKnowledgeGraph> getGraph(@PathVariable UUID enrollmentId) {
        return ApiResponse.success("Learning knowledge graph retrieved",
                service.forEnrollment(enrollmentId, SecurityUtils.getCurrentUserId()));
    }
}
