package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.AdaptiveFollowUpRecommendation;
import com.masterlearning.platform.modules.ai.dto.response.LearnerTutorContext;
import com.masterlearning.platform.modules.ai.dto.response.TutorIntelligenceContext;
import com.masterlearning.platform.modules.ai.dto.response.UnderstandingEvaluation;
import org.springframework.stereotype.Service;

@Service
public class AdaptiveFollowUpRecommendationService {

    public AdaptiveFollowUpRecommendation recommend(LearnerTutorContext learner,
                                                     TutorIntelligenceContext intelligence,
                                                     UnderstandingEvaluation evaluation) {
        if (evaluation != null && "CONFUSED".equals(evaluation.signal())) {
            return new AdaptiveFollowUpRecommendation(
                    "SOCRATIC_SCAFFOLD",
                    "GUIDED_STEP",
                    "EASY",
                    "REBUILD_UNDERSTANDING",
                    true);
        }

        if (intelligence != null && intelligence.signals().contains("prerequisite_blocker")) {
            return new AdaptiveFollowUpRecommendation(
                    "PREREQUISITE_CHECK",
                    "FOUNDATION_CHECK",
                    "EASY",
                    "VERIFY_PREREQUISITE",
                    true);
        }

        if (evaluation != null && "UNCERTAIN".equals(evaluation.signal())) {
            return new AdaptiveFollowUpRecommendation(
                    "SOCRATIC_CHECK",
                    "WHY_OR_HOW",
                    "MEDIUM",
                    "RESOLVE_UNCERTAINTY",
                    true);
        }

        if (evaluation != null && "ENGAGED".equals(evaluation.signal())) {
            return new AdaptiveFollowUpRecommendation(
                    "PRACTICE_CHECK",
                    "APPLICATION",
                    learner.masteryScore() >= 85.0 ? "HARD" : "MEDIUM",
                    "APPLY_CONCEPT",
                    true);
        }

        if (learner.masteryScore() >= 85.0) {
            return new AdaptiveFollowUpRecommendation(
                    "CHALLENGE",
                    "TRANSFER",
                    "HARD",
                    "DEEPEN_AND_TRANSFER",
                    false);
        }

        return new AdaptiveFollowUpRecommendation(
                "SOCRATIC_CHECK",
                "CONCEPT_CHECK",
                learner.masteryScore() < 50.0 ? "EASY" : "MEDIUM",
                "CHECK_UNDERSTANDING",
                true);
    }
}
