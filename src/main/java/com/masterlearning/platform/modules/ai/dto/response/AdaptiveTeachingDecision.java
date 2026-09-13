package com.masterlearning.platform.modules.ai.dto.response;

import java.util.List;

public record AdaptiveTeachingDecision(
        String intervention,
        String teachingStrategy,
        String explanationLevel,
        String difficulty,
        String followUpMode,
        boolean practiceRecommended,
        boolean prerequisiteBlocked,
        List<String> reasons
) {}
