package com.masterlearning.platform.modules.assessment.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.assessment.dto.response.LearnerPersonalization;
import com.masterlearning.platform.modules.assessment.service.LearnerPersonalizationService;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning/enrollments")
public class LearnerPersonalizationController {
    private final LearnerPersonalizationService service;

    public LearnerPersonalizationController(LearnerPersonalizationService service) {
        this.service = service;
    }

    @GetMapping("/{enrollmentId}/personalization")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<LearnerPersonalization> personalization(@PathVariable UUID enrollmentId) {
        return ApiResponse.success("Learner personalization generated",
                service.forEnrollment(enrollmentId, SecurityUtils.getCurrentUserId()));
    }
}
