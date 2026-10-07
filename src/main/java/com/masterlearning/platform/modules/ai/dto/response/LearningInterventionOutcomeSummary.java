package com.masterlearning.platform.modules.ai.dto.response;

public record LearningInterventionOutcomeSummary(
        long total,
        long successful,
        long failed,
        long noImprovement,
        long unknown,
        double averageMasteryDelta
) {}