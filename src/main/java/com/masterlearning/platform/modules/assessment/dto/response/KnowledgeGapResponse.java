package com.masterlearning.platform.modules.assessment.dto.response;

import java.util.List;

public record KnowledgeGapResponse(
        int totalEvaluatedQuestions,
        int weakQuestions,
        double averageAccuracyPercent,
        String overallStatus,
        List<KnowledgeGapItemResponse> gaps) {
}
