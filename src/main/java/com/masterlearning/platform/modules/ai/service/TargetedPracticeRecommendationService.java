package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.LearnerTutorContext;
import com.masterlearning.platform.modules.ai.dto.response.TargetedPracticeRecommendation;
import com.masterlearning.platform.modules.ai.dto.response.TutorIntelligenceContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TargetedPracticeRecommendationService {

    public TargetedPracticeRecommendation recommend(LearnerTutorContext learner,
                                                     TutorIntelligenceContext intelligence) {
        List<String> reasons = new ArrayList<>();
        boolean weakConcept = learner.weakConcepts() != null && !learner.weakConcepts().isEmpty();
        boolean prerequisite = intelligence.signals().contains("prerequisite_blocker");
        boolean misconception = intelligence.signals().contains("possible_misconception");
        boolean practice = intelligence.practiceRecommended();

        if (prerequisite) reasons.add("prerequisite_gap");
        if (misconception) reasons.add("misconception_repair");
        if (weakConcept) reasons.add("weak_concept");
        if (practice) reasons.add("practice_signal");

        if (!practice && learner.masteryScore() >= 85.0) {
            return new TargetedPracticeRecommendation("LOW", "CHALLENGE", "HARD", "ADVANCEMENT", false, List.of("advanced_mastery"));
        }

        String priority = prerequisite || misconception || learner.masteryScore() < 50.0 ? "HIGH" : "MEDIUM";
        String type = prerequisite ? "PREREQUISITE_PRACTICE"
                : misconception ? "MISCONCEPTION_CHECK"
                : learner.masteryScore() < 50.0 ? "SCAFFOLDED_PRACTICE"
                : "TARGETED_PRACTICE";
        String difficulty = learner.masteryScore() < 50.0 ? "EASY"
                : learner.masteryScore() >= 85.0 ? "HARD" : "MEDIUM";
        String focus = weakConcept ? learner.weakConcepts().get(0)
                : prerequisite ? "PREREQUISITE_CONCEPT"
                : misconception ? "MISCONCEPTION_CHECK"
                : "CURRENT_CONCEPT";

        return new TargetedPracticeRecommendation(priority, type, difficulty, focus, true, List.copyOf(reasons));
    }
}
