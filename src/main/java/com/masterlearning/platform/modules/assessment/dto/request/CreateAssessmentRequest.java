package com.masterlearning.platform.modules.assessment.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateAssessmentRequest(
        @NotBlank String title,
        @Min(1) @Max(100) int passingScore,
        @Min(1) @Max(100) Integer maxAttempts
) {}