package com.masterlearning.platform.modules.learning.dto.response;

import java.util.List;
import java.util.UUID;

public record PersonalizedLearningPath(
        UUID enrollmentId,
        UUID courseId,
        String strategy,
        List<Step> steps,
        List<String> reasons
) {
    public record Step(UUID lessonId, String title, String action, int priority, String reason) {}
}
