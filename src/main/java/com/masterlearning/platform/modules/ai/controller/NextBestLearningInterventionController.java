package com.masterlearning.platform.modules.ai.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.ai.dto.response.NextBestLearningIntervention;
import com.masterlearning.platform.modules.ai.service.NextBestLearningInterventionService;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning/enrollments/{enrollmentId}/ai")
public class NextBestLearningInterventionController {
    private final NextBestLearningInterventionService service;

    public NextBestLearningInterventionController(NextBestLearningInterventionService service) {
        this.service = service;
    }

    @GetMapping("/next-best-learning")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<NextBestLearningIntervention> nextBestLearning(@PathVariable UUID enrollmentId) {
        return ApiResponse.success("Next-best-learning intervention generated",
                service.forEnrollment(enrollmentId, SecurityUtils.getCurrentUserId()));
    }
}
