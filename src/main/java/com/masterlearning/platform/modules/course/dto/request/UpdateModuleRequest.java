package com.masterlearning.platform.modules.course.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateModuleRequest(
        @NotBlank String title,
        @Min(0) int sortOrder
) {}
