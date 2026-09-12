package com.masterlearning.platform.modules.ai.dto.response;

import java.util.List;

public record TutorIntelligenceContext(
        String misconceptionSignal,
        String teachingStrategy,
        String followUpMode,
        String explanationLevel,
        boolean practiceRecommended,
        List<String> signals
) {}
