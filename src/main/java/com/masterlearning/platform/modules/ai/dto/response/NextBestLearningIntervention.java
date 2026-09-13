package com.masterlearning.platform.modules.ai.dto.response;

import java.util.List;
import java.util.UUID;

public record NextBestLearningIntervention(
        UUID enrollmentId,
        UUID courseId,
        String intervention,
        String priority,
        UUID targetLessonId,
        String targetLessonTitle,
        String rationale,
        String expectedOutcome,
        List<String> reasons
) {}
