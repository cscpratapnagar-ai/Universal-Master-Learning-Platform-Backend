package com.masterlearning.platform.modules.learning.dto.response;

import java.util.List;
import java.util.UUID;

public record PersonalizedRecommendation(
        UUID enrollmentId,
        UUID courseId,
        String action,
        String priority,
        UUID targetLessonId,
        String targetLessonTitle,
        String rationale,
        List<String> signals
) {}
