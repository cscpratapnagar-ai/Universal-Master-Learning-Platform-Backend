package com.masterlearning.platform.modules.ai.dto.response;

import java.util.List;
import java.util.UUID;

public record AiLearningOrchestration(
        UUID enrollmentId,
        UUID courseId,
        String learnerState,
        String recommendedAction,
        String teachingStrategy,
        String assessmentAction,
        String priority,
        List<String> reasons
) {}
