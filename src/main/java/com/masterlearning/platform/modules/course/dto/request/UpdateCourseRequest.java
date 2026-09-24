package com.masterlearning.platform.modules.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateCourseRequest(
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 220) String slug,
        @Size(max = 2000) String description,
        UUID organizationId
) {}
