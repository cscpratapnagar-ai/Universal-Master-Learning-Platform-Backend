package com.masterlearning.platform.modules.learning.dto.response;

import java.util.List;
import java.util.UUID;

public record PersonalizedDifficultyPace(
        UUID enrollmentId,
        UUID courseId,
        String difficulty,
        String pace,
        int recommendedDailyLessons,
        String rationale,
        List<String> signals
) {}
