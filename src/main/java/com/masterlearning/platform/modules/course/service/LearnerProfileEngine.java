package com.masterlearning.platform.modules.course.service;

import com.masterlearning.platform.modules.course.dto.response.LearnerProfileResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class LearnerProfileEngine {
    public LearnerProfileResponse build(UUID enrollmentId, UUID userId, int completionPercent,
                                        long completedLessons, double masteryScore,
                                        String masteryLevel, String momentum,
                                        long totalAttempts, long passedAttempts,
                                        boolean hasKnowledgeGaps, boolean assessmentDeclining) {
        double mastery = Math.max(0, Math.min(100, masteryScore));
        String risk;
        if (completionPercent == 0 && totalAttempts == 0) risk = "NEW";
        else if (assessmentDeclining || (hasKnowledgeGaps && mastery < 50)) risk = "HIGH";
        else if (mastery < 60 || completionPercent < 30) risk = "MEDIUM";
        else risk = "LOW";

        String state;
        if (completionPercent >= 100) state = "COMPLETED";
        else if (risk.equals("HIGH")) state = "NEEDS_SUPPORT";
        else if (mastery >= 85 && completionPercent >= 50) state = "ACCELERATING";
        else if (completedLessons > 0) state = "ACTIVE";
        else state = "STARTING";

        String action;
        if (risk.equals("HIGH")) action = "REMEDIATE_AND_REASSESS";
        else if (mastery >= 85) action = "ACCELERATE";
        else if (mastery < 60) action = "TARGETED_PRACTICE";
        else action = "CONTINUE_LEARNING";

        List<String> strengths = mastery >= 80 ? List.of("Strong assessment mastery") : List.of();
        List<String> focus = hasKnowledgeGaps ? List.of("Knowledge gaps") : mastery < 60 ? List.of("Mastery development") : List.of();
        return new LearnerProfileResponse(enrollmentId, userId, state, masteryLevel,
                round(mastery), Math.max(0, Math.min(100, completionPercent)), momentum, risk,
                action, strengths, focus);
    }

    private double round(double value) { return Math.round(value * 10.0) / 10.0; }
}
