package com.masterlearning.platform.modules.course.dto.response;

import java.time.Instant;
import java.util.UUID;

public record LearningAnalyticsResponse(
        UUID enrollmentId,
        int completionPercent,
        int completedLessons,
        int totalLessons,
        double masteryScore,
        double learningVelocity,
        double projectedCompletionPercent30Days,
        String completionPrediction,
        String riskLevel,
        String engagementState,
        Instant lastActivityAt,
        String insight) {
}
