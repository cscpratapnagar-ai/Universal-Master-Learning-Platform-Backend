package com.masterlearning.platform.modules.assessment.service;

import com.masterlearning.platform.modules.assessment.dto.response.KnowledgeGapItemResponse;
import com.masterlearning.platform.modules.assessment.dto.response.KnowledgeGapResponse;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAnswerRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class KnowledgeGapEngine {

    public KnowledgeGapResponse analyze(List<AssessmentAnswerRepository.KnowledgeGapProjection> evidence) {
        Map<UUID, AssessmentAnswerRepository.KnowledgeGapProjection> latestByQuestion = new LinkedHashMap<>();
        for (var item : evidence) {
            latestByQuestion.putIfAbsent(item.getQuestionId(), item);
        }

        if (latestByQuestion.isEmpty()) {
            return new KnowledgeGapResponse(0, 0, 0.0, "NO_DATA", List.of());
        }

        double accuracy = latestByQuestion.values().stream()
                .mapToDouble(item -> item.getMaxPoints() <= 0 ? (item.isCorrect() ? 1.0 : 0.0)
                        : Math.max(0.0, Math.min(1.0, (double) item.getPointsAwarded() / item.getMaxPoints())))
                .average().orElse(0.0);

        List<KnowledgeGapItemResponse> gaps = latestByQuestion.values().stream()
                .filter(item -> !item.isCorrect() || item.getPointsAwarded() < item.getMaxPoints())
                .map(item -> {
                    double questionAccuracy = item.getMaxPoints() <= 0
                            ? (item.isCorrect() ? 1.0 : 0.0)
                            : Math.max(0.0, Math.min(1.0, (double) item.getPointsAwarded() / item.getMaxPoints()));
                    double severity = (1.0 - questionAccuracy) * 100.0;
                    String priority = severity >= 75 ? "HIGH" : severity >= 40 ? "MEDIUM" : "LOW";
                    return new KnowledgeGapItemResponse(
                            item.getQuestionId(), item.getQuestionText(), item.getAssessmentTitle(),
                            item.getLessonId(), item.isCorrect(), item.getPointsAwarded(), item.getMaxPoints(),
                            round(severity), priority, item.getSubmittedAt());
                })
                .sorted(Comparator.comparing(KnowledgeGapItemResponse::gapSeverity).reversed())
                .toList();

        String status = accuracy >= 0.80 ? "STRONG" : accuracy >= 0.60 ? "DEVELOPING" : "AT_RISK";
        return new KnowledgeGapResponse(
                latestByQuestion.size(), gaps.size(), round(accuracy * 100.0), status, gaps);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
