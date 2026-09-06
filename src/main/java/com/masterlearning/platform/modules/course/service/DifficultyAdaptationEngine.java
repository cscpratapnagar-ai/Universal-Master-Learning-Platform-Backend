package com.masterlearning.platform.modules.course.service;

import com.masterlearning.platform.modules.course.dto.response.DifficultyAdaptationResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DifficultyAdaptationEngine {
    public DifficultyAdaptationResponse decide(UUID enrollmentId, double masteryScore,
            int consecutivePasses, int consecutiveFailures) {
        double mastery = normalize(masteryScore);
        if (consecutiveFailures >= 2 || mastery < 50) {
            return new DifficultyAdaptationResponse(enrollmentId, "EASY", "DECREASE",
                    consecutiveFailures >= 2 ? "Repeated failures" : "Low mastery", mastery,
                    Math.max(0, consecutivePasses), Math.max(0, consecutiveFailures));
        }
        if (consecutivePasses >= 3 || mastery >= 85) {
            return new DifficultyAdaptationResponse(enrollmentId, "HARD", "INCREASE",
                    consecutivePasses >= 3 ? "Repeated success" : "Strong mastery", mastery,
                    Math.max(0, consecutivePasses), Math.max(0, consecutiveFailures));
        }
        return new DifficultyAdaptationResponse(enrollmentId, "MEDIUM", "HOLD",
                "Performance is within the adaptive range", mastery,
                Math.max(0, consecutivePasses), Math.max(0, consecutiveFailures));
    }

    private double normalize(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return 0.0;
        return Math.max(0.0, Math.min(100.0, Math.round(value * 10.0) / 10.0));
    }
}
