package com.masterlearning.platform.modules.ai.dto.response;

import java.util.List;

public record SocraticTeachingPlan(
        String strategy,
        String promptStyle,
        String questionType,
        int guidanceLevel,
        boolean revealAnswer,
        List<String> steps
) {}
