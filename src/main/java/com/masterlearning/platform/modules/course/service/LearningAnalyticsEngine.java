package com.masterlearning.platform.modules.course.service;

import com.masterlearning.platform.modules.course.dto.response.LearningAnalyticsResponse;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class LearningAnalyticsEngine {

    public LearningAnalyticsResponse analyze(
            UUID enrollmentId,
            int completedLessons,
            int totalLessons,
            double masteryScore,
            long learningSeconds,
            Instant enrollmentCreatedAt,
            Instant lastActivityAt) {

        int safeTotal = Math.max(0, totalLessons);
        int safeCompleted = Math.max(0, Math.min(completedLessons, safeTotal));
        double completion = safeTotal == 0 ? 0 : safeCompleted * 100.0 / safeTotal;

        long elapsedDays = enrollmentCreatedAt == null
                ? 1
                : Math.max(1, Duration.between(enrollmentCreatedAt, Instant.now()).toDays() + 1);
        double velocity = safeCompleted / (double) elapsedDays;
        double projected30 = Math.min(100.0, completion + velocity * 30.0 * 100.0 / Math.max(1, safeTotal));

        String prediction = safeTotal == 0 || safeCompleted >= safeTotal
                ? "COMPLETE"
                : projected30 >= 100 ? "ON_TIME" : projected30 >= 75 ? "LIKELY" : "AT_RISK";

        long inactiveDays = lastActivityAt == null
                ? Long.MAX_VALUE
                : Math.max(0, Duration.between(lastActivityAt, Instant.now()).toDays());
        String engagement = lastActivityAt == null ? "NOT_STARTED"
                : inactiveDays <= 2 ? "ACTIVE"
                : inactiveDays <= 7 ? "COOLING_DOWN" : "INACTIVE";

        String risk = prediction.equals("AT_RISK") || engagement.equals("INACTIVE") || masteryScore < 50
                ? "HIGH"
                : prediction.equals("LIKELY") || engagement.equals("COOLING_DOWN") || masteryScore < 70
                ? "MEDIUM" : "LOW";

        String insight = safeCompleted >= safeTotal && safeTotal > 0
                ? "Course completion target achieved"
                : risk.equals("HIGH")
                ? "Increase learning consistency and address mastery gaps"
                : velocity > 0
                ? "Learning pace is progressing toward completion"
                : "Start completing lessons to establish a learning trajectory";

        return new LearningAnalyticsResponse(
                enrollmentId,
                (int) Math.round(completion),
                safeCompleted,
                safeTotal,
                round(masteryScore),
                round(velocity),
                round(projected30),
                prediction,
                risk,
                engagement,
                lastActivityAt,
                insight);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
