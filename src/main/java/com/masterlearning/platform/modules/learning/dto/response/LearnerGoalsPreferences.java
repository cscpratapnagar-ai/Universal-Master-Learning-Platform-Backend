package com.masterlearning.platform.modules.learning.dto.response;

import java.util.List;
import java.util.UUID;

public record LearnerGoalsPreferences(
        UUID enrollmentId,
        UUID courseId,
        String primaryGoal,
        String targetOutcome,
        String explanationStyle,
        String pacePreference,
        List<String> studyPreferences,
        List<String> personalizationSignals
) {}
