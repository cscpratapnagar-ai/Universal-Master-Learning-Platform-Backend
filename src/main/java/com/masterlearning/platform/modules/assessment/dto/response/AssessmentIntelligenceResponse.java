package com.masterlearning.platform.modules.assessment.dto.response;

import java.util.List;
import java.util.UUID;

public record AssessmentIntelligenceResponse(
        UUID enrollmentId,
        String readiness,
        String trend,
        double averageScore,
        double latestScore,
        int totalAttempts,
        int passedAttempts,
        boolean retakeRecommended,
        String recommendation,
        List<AssessmentInsight> assessments) {
    public record AssessmentInsight(UUID assessmentId, String title, String level,
                                    int attempts, int latestScore, boolean latestPassed,
                                    String trend, String readiness, String recommendation) {}
}
