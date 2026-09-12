package com.masterlearning.platform.modules.assessment.dto.response;

import java.util.List;
import java.util.UUID;

public record AdaptiveAssessmentDecision(
        UUID assessmentId,
        UUID selectedQuestionId,
        String action,
        String difficulty,
        String focus,
        double learnerMastery,
        List<String> reasons
) {}
