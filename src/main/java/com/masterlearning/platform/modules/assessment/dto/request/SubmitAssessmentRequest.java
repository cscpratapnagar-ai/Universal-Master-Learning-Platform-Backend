package com.masterlearning.platform.modules.assessment.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record SubmitAssessmentRequest(
        @NotNull Map<UUID, UUID> answers
) {
}
