package com.masterlearning.platform.modules.learning.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.learning.dto.response.PersonalizationOrchestration;
import com.masterlearning.platform.modules.learning.service.PersonalizationOrchestrationService;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning/enrollments/{enrollmentId}/personalization")
public class PersonalizationOrchestrationController {
    private final PersonalizationOrchestrationService service;

    public PersonalizationOrchestrationController(PersonalizationOrchestrationService service) {
        this.service = service;
    }

    @GetMapping("/orchestration")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PersonalizationOrchestration> orchestrate(@PathVariable UUID enrollmentId) {
        return ApiResponse.success("Personalization decision orchestrated",
                service.forEnrollment(enrollmentId, SecurityUtils.getCurrentUserId()));
    }
}
