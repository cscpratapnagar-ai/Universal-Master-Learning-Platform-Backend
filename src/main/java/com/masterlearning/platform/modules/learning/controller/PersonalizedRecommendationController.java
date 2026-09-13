package com.masterlearning.platform.modules.learning.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.learning.dto.response.PersonalizedRecommendation;
import com.masterlearning.platform.modules.learning.service.PersonalizedRecommendationService;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/learning/enrollments/{enrollmentId}/personalization")
public class PersonalizedRecommendationController {
    private final PersonalizedRecommendationService service;

    public PersonalizedRecommendationController(PersonalizedRecommendationService service) {
        this.service = service;
    }

    @GetMapping("/recommendation")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PersonalizedRecommendation> getRecommendation(@PathVariable UUID enrollmentId) {
        return ApiResponse.success("Personalized learning recommendation generated",
                service.forEnrollment(enrollmentId, SecurityUtils.getCurrentUserId()));
    }
}
