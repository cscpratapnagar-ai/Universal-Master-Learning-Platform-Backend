package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.AdaptiveExplanationPlan;
import com.masterlearning.platform.modules.ai.dto.response.LearnerTutorContext;
import com.masterlearning.platform.modules.ai.dto.response.TutorIntelligenceContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdaptiveExplanationPlanService {

    public AdaptiveExplanationPlan plan(LearnerTutorContext learner, TutorIntelligenceContext intelligence) {
        List<String> reasons = new ArrayList<>();
        String level;
        if ("PREREQUISITE_BRIDGE".equals(intelligence.teachingStrategy())
                || "FOUNDATIONAL".equals(intelligence.explanationLevel())) {
            level = "FOUNDATIONAL";
            reasons.add("foundation_needed");
        } else if ("MISCONCEPTION_REPAIR".equals(intelligence.teachingStrategy())
                || "SOCRATIC_GUIDANCE".equals(intelligence.teachingStrategy())) {
            level = "INTERMEDIATE";
            reasons.add("guided_understanding_needed");
        } else if (learner.masteryScore() >= 85.0) {
            level = "ADVANCED";
            reasons.add("advanced_mastery");
        } else {
            level = "INTERMEDIATE";
            reasons.add("balanced_learning_level");
        }

        boolean stepByStep = "FOUNDATIONAL".equals(level)
                || intelligence.signals().contains("prerequisite_blocker")
                || intelligence.signals().contains("explicit_confusion");
        boolean example = stepByStep || intelligence.practiceRecommended();
        boolean analogy = "FOUNDATIONAL".equals(level) || "SOCRATIC_GUIDANCE".equals(intelligence.teachingStrategy());
        String depth = "ADVANCED".equals(level) ? "DEEP" : "GUIDED";
        String structure = stepByStep ? "STEP_BY_STEP" : "CONCEPT_THEN_APPLICATION";

        return new AdaptiveExplanationPlan(level, depth, structure, example, analogy, stepByStep, List.copyOf(reasons));
    }
}
