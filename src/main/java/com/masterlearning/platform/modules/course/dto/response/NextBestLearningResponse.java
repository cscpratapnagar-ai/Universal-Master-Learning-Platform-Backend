package com.masterlearning.platform.modules.course.dto.response;

import java.util.List;
import java.util.UUID;

public record NextBestLearningResponse(
        UUID enrollmentId,
        String learnerState,
        String recommendationType,
        UUID recommendedLessonId,
        String recommendedLessonTitle,
        double score,
        String reason,
        List<Alternative> alternatives) {
    public record Alternative(UUID lessonId, String title, String reason, double score) {}
}
