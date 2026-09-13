package com.masterlearning.platform.modules.assessment.dto.response;

import java.util.List;
import java.util.UUID;

public record AdaptiveAssessmentMasteryLoop(
        UUID sessionId,
        UUID assessmentId,
        int score,
        boolean passed,
        String masteryLevel,
        String nextAction,
        double courseMastery,
        int weakConceptCount,
        List<String> reasons
) {}
