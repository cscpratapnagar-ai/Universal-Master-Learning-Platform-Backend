package com.masterlearning.platform.modules.ai.dto.response;

public record AdaptiveFollowUpRecommendation(
        String mode,
        String questionType,
        String difficulty,
        String objective,
        boolean required
) {}
