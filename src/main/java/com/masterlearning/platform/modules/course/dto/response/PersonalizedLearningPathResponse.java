package com.masterlearning.platform.modules.course.dto.response;

import java.util.List;
import java.util.UUID;

public record PersonalizedLearningPathResponse(
        UUID enrollmentId,
        String learnerState,
        double masteryScore,
        int totalLessons,
        int completedLessons,
        int remainingLessons,
        List<PathItem> path) {
    public record PathItem(UUID lessonId, String title, int sequence, String action,
                           String reason, double priority, boolean prerequisite) {}
}
