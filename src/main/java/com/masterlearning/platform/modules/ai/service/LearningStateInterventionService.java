package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.LearningStateIntervention;
import com.masterlearning.platform.modules.ai.dto.response.LearnerTutorContext;
import com.masterlearning.platform.modules.ai.dto.response.TargetedPracticeRecommendation;
import com.masterlearning.platform.modules.ai.dto.response.TutorIntelligenceContext;
import com.masterlearning.platform.modules.ai.dto.response.AdaptiveExplanationPlan;
import com.masterlearning.platform.modules.ai.dto.response.UnderstandingEvaluation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LearningStateInterventionService {

    public LearningStateIntervention decide(LearnerTutorContext learner,
                                             TutorIntelligenceContext intelligence,
                                             AdaptiveExplanationPlan explanation,
                                             TargetedPracticeRecommendation practice,
                                             UnderstandingEvaluation understanding) {
        List<String> reasons = new ArrayList<>();
        String intervention;
        String strategy = intelligence.teachingStrategy();
        String level = explanation.level();
        String followUp = understanding.nextAction();
        boolean practiceRecommended = practice.recommended() || understanding.needsPractice();

        if ("CONFUSED".equals(understanding.signal())) {
            intervention = "RETEACH_AND_CHECK";
            reasons.add("learner_confusion");
        } else if ("UNCERTAIN".equals(understanding.signal())) {
            intervention = "GUIDED_CHECK";
            reasons.add("learner_uncertainty");
        } else if (intelligence.signals().contains("prerequisite_blocker")) {
            intervention = "BRIDGE_PREREQUISITE";
            reasons.add("prerequisite_gap");
        } else if (intelligence.signals().contains("possible_misconception")) {
            intervention = "REPAIR_MISCONCEPTION";
            reasons.add("possible_misconception");
        } else if (learner.masteryScore() >= 85.0) {
            intervention = "DEEPEN_AND_CHALLENGE";
            reasons.add("advanced_mastery");
        } else {
            intervention = "TEACH_AND_PRACTICE";
            reasons.add("learning_progression");
        }

        if (practiceRecommended) reasons.add("practice_needed");
        if (learner.weakConcepts() != null && !learner.weakConcepts().isEmpty()) reasons.add("weak_concept_context");

        return new LearningStateIntervention(intervention, strategy, level, followUp, practiceRecommended, List.copyOf(reasons));
    }
}
