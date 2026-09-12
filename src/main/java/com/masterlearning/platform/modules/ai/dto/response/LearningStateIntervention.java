package com.masterlearning.platform.modules.ai.dto.response;

import java.util.List;

public record LearningStateIntervention(
        String intervention,
        String teachingStrategy,
        String explanationLevel,
        String followUpMode,
        boolean practiceRecommended,
        List<String> reasons
) {}
