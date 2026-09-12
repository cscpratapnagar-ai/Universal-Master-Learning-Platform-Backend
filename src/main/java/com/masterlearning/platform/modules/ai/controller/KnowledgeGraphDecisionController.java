package com.masterlearning.platform.modules.ai.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.ai.dto.response.KnowledgeGraphDecision;
import com.masterlearning.platform.modules.ai.service.KnowledgeGraphDecisionService;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning/enrollments/{enrollmentId}/knowledge-graph")
public class KnowledgeGraphDecisionController {
    private final KnowledgeGraphDecisionService service;

    public KnowledgeGraphDecisionController(KnowledgeGraphDecisionService service) {
        this.service = service;
    }

    @GetMapping("/decision")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<KnowledgeGraphDecision> decide(@PathVariable UUID enrollmentId) {
        return ApiResponse.success("Knowledge graph learning decision generated",
                service.decide(enrollmentId, SecurityUtils.getCurrentUserId()));
    }
}
