package com.masterlearning.platform.modules.assessment.dto.response;

import java.util.List;
import java.util.UUID;

public record LearnerPersonalization(
        UUID enrollmentId,
        UUID courseId,
        double mastery,
        String learnerState,
        String riskLevel,
        String momentum,
        String priority,
        String nextAction,
        List<String> focusAreas,
        List<String> reasons
) {}
