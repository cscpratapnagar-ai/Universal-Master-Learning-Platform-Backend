package com.masterlearning.platform.modules.assessment.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AdaptiveAssessmentAnswerRequest(@NotNull UUID questionId, UUID selectedOptionId) {}
