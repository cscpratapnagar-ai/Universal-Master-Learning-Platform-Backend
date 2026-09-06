package com.masterlearning.platform.modules.ai.service;

import org.springframework.stereotype.Service;

@Service
public class AdaptiveTutorContextService {
    public String normalizeQuestion(String question) {
        if (question == null) return "";
        return question.trim().replaceAll("\\s+", " ");
    }

    public String explanationStyle(double masteryScore) {
        if (masteryScore < 50) return "SIMPLE_STEP_BY_STEP";
        if (masteryScore >= 85) return "CHALLENGE_AND_ACCELERATE";
        return "BALANCED_EXPLANATION";
    }
}
