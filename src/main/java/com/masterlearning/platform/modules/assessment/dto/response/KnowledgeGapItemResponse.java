package com.masterlearning.platform.modules.assessment.dto.response;

import java.time.Instant;
import java.util.UUID;

public record KnowledgeGapItemResponse(
        UUID questionId,
        String questionText,
        String assessmentTitle,
        UUID lessonId,
        boolean correct,
        int pointsAwarded,
        int maxPoints,
        double gapSeverity,
        String remediationPriority,
        Instant lastAttemptedAt) {
}
