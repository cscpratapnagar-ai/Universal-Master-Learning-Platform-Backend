package com.masterlearning.platform.modules.ai.dto.response;

import java.util.List;
import java.util.UUID;

public record AiTutorOrchestration(
        UUID enrollmentId,
        UUID courseId,
        String learnerState,
        String intervention,
        String teachingStrategy,
        String explanationLevel,
        String difficulty,
        String followUpMode,
        boolean practiceRecommended,
        boolean prerequisiteBlocked,
        List<String> reasons
) {}
