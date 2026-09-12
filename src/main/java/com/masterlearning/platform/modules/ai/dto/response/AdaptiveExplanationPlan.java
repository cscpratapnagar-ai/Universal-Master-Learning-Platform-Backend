package com.masterlearning.platform.modules.ai.dto.response;

import java.util.List;

public record AdaptiveExplanationPlan(
        String level,
        String depth,
        String structure,
        boolean useExample,
        boolean useAnalogy,
        boolean useStepByStep,
        List<String> reasons
) {}
