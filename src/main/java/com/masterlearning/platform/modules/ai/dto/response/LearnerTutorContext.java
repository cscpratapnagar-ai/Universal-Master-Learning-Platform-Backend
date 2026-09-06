package com.masterlearning.platform.modules.ai.dto.response;

import java.util.List;
import java.util.UUID;

public record LearnerTutorContext(
        UUID enrollmentId,
        double masteryScore,
        String learnerState,
        String riskLevel,
        String momentum,
        List<String> weakAreas,
        String recommendedAction,
        String explanationStyle
) {}
