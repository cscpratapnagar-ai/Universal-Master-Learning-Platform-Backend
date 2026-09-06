package com.masterlearning.platform.modules.assessment.service;

import com.masterlearning.platform.modules.assessment.dto.response.AssessmentIntelligenceResponse;
import com.masterlearning.platform.modules.assessment.entity.Assessment;
import com.masterlearning.platform.modules.assessment.entity.AssessmentAttempt;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class AssessmentIntelligenceEngine {
    public AssessmentIntelligenceResponse analyze(UUID enrollmentId, List<AssessmentAttempt> attempts) {
        List<AssessmentAttempt> evidence = attempts == null ? List.of() : attempts.stream()
                .filter(Objects::nonNull).sorted(Comparator.comparing(AssessmentAttempt::getSubmittedAt).reversed()).toList();
        if (evidence.isEmpty()) {
            return new AssessmentIntelligenceResponse(enrollmentId, "NOT_READY", "NO_DATA", 0, 0, 0, 0,
                    false, "Complete an assessment to establish a readiness baseline", List.of());
        }

        double average = evidence.stream().mapToInt(AssessmentAttempt::getScore).average().orElse(0);
        int latest = evidence.getFirst().getScore();
        int passed = (int) evidence.stream().filter(AssessmentAttempt::isPassed).count();

        Map<UUID, List<AssessmentAttempt>> byAssessment = evidence.stream()
                .collect(Collectors.groupingBy(a -> a.getAssessment().getId(), LinkedHashMap::new, Collectors.toList()));
        List<AssessmentIntelligenceResponse.AssessmentInsight> insights = byAssessment.values().stream()
                .map(this::insight).toList();

        String readiness = average >= 80 ? "READY" : average >= 60 ? "NEAR_READY" : "NOT_READY";
        String trend = trend(evidence);
        boolean retake = insights.stream().anyMatch(i -> !i.latestPassed() && i.attempts() < i.assessmentId().toString().length() + 1000);
        String recommendation = readiness.equals("READY") ? "Proceed to the next learning challenge"
                : trend.equals("DECLINING") ? "Review weak areas before attempting the next assessment"
                : "Use targeted practice and reassess before advancing";
        return new AssessmentIntelligenceResponse(enrollmentId, readiness, trend, round(average), latest,
                evidence.size(), passed, retake, recommendation, insights);
    }

    private AssessmentIntelligenceResponse.AssessmentInsight insight(List<AssessmentAttempt> attempts) {
        AssessmentAttempt latest = attempts.getFirst();
        Assessment a = latest.getAssessment();
        String trend = trend(attempts);
        String readiness = latest.getScore() >= 80 ? "READY" : latest.isPassed() ? "NEAR_READY" : "NOT_READY";
        String recommendation = latest.isPassed() ? (trend.equals("IMPROVING") ? "Advance with confidence" : "Maintain practice")
                : attempts.size() >= a.getMaxAttempts() ? "Review remediation before another attempt" : "Review weak areas and retake";
        return new AssessmentIntelligenceResponse.AssessmentInsight(a.getId(), a.getTitle(), a.getAssessmentLevel(),
                attempts.size(), latest.getScore(), latest.isPassed(), trend, readiness, recommendation);
    }

    private String trend(List<AssessmentAttempt> attempts) {
        if (attempts.size() < 2) return "STABLE";
        int latest = attempts.get(0).getScore();
        int previous = attempts.get(1).getScore();
        if (latest >= previous + 5) return "IMPROVING";
        if (latest <= previous - 5) return "DECLINING";
        return "STABLE";
    }

    private double round(double value) { return Math.round(value * 100.0) / 100.0; }
}
