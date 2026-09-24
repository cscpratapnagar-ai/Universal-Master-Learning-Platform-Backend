package com.masterlearning.platform.modules.course.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateLessonRequest(
        @NotBlank @Size(max = 220) String title,
        @Size(max = 30) String contentType,
        @Size(max = 4000) String content,
        @Min(0) int sortOrder
) {}
