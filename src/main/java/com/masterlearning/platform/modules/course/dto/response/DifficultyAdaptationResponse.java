package com.masterlearning.platform.modules.course.dto.response;

import java.util.UUID;

public record DifficultyAdaptationResponse(
        UUID enrollmentId,
        String difficulty,
        String direction,
        String reason,
        double masteryScore,
        int consecutivePasses,
        int consecutiveFailures) {}
