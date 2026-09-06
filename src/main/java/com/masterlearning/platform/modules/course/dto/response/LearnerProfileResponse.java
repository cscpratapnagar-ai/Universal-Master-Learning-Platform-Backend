package com.masterlearning.platform.modules.course.dto.response;

import java.util.List;
import java.util.UUID;

public record LearnerProfileResponse(
        UUID enrollmentId,
        UUID userId,
        String learnerState,
        String masteryLevel,
        double masteryScore,
        int completionPercent,
        String learningMomentum,
        String riskLevel,
        String recommendedAction,
        List<String> strengths,
        List<String> focusAreas) {
}
