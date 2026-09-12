package com.masterlearning.platform.modules.ai.dto.response;

import java.util.List;

public record TargetedPracticeRecommendation(
        String priority,
        String practiceType,
        String difficulty,
        String focus,
        boolean recommended,
        List<String> reasons
) {}
