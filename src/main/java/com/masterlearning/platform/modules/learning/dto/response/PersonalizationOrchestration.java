package com.masterlearning.platform.modules.learning.dto.response;

import java.util.List;
import java.util.UUID;

public record PersonalizationOrchestration(
        UUID enrollmentId,
        UUID courseId,
        String finalAction,
        String priority,
        UUID targetLessonId,
        String targetLessonTitle,
        String difficulty,
        String pace,
        String primaryGoal,
        String rationale,
        List<String> signals
) {}
