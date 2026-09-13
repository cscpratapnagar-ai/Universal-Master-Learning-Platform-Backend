package com.masterlearning.platform.modules.ai.dto.response;

import java.util.List;
import java.util.UUID;

public record AdaptiveAssessmentOrchestration(
        UUID enrollmentId,
        UUID courseId,
        String readiness,
        String action,
        String difficulty,
        String focus,
        boolean assessmentRecommended,
        List<String> reasons
) {}
