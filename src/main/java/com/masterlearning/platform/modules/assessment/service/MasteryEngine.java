package com.masterlearning.platform.modules.assessment.service;

import org.springframework.stereotype.Component;

@Component
public class MasteryEngine {

    public String masteryLevel(int score) {
        return masteryLevel((double) score);
    }

    public String masteryLevel(double score) {
        if (score >= 90) return "MASTERED";
        if (score >= 70) return "PROFICIENT";
        if (score >= 50) return "DEVELOPING";
        if (score > 0) return "EMERGING";
        return "NOT_ASSESSED";
    }

    public String assessmentMasteryLevel(int score) {
        if (score >= 90) return "MASTERED";
        if (score >= 70) return "PROFICIENT";
        if (score >= 50) return "DEVELOPING";
        return "NEEDS_REVIEW";
    }

    public String assessmentOutcome(int score, int passingScore) {
        return score >= passingScore ? "PASSED" : "NEEDS_REVIEW";
    }

    public String momentum(int completionPercent, double masteryScore, long completedLessons) {
        if (completionPercent >= 80 && masteryScore >= 80) return "EXCELLENT";
        if (completionPercent >= 50 || masteryScore >= 70) return "ON_TRACK";
        if (completedLessons > 0) return "BUILDING";
        return "STARTING";
    }
}
