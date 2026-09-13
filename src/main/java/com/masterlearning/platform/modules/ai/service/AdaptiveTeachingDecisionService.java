package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.AdaptiveExplanationPlan;
import com.masterlearning.platform.modules.ai.dto.response.AdaptiveFollowUpRecommendation;
import com.masterlearning.platform.modules.ai.dto.response.AdaptiveTeachingDecision;
import com.masterlearning.platform.modules.ai.dto.response.LearningStateIntervention;
import com.masterlearning.platform.modules.ai.dto.response.LearnerTutorContext;
import com.masterlearning.platform.modules.ai.dto.response.TargetedPracticeRecommendation;
import com.masterlearning.platform.modules.ai.dto.response.TutorIntelligenceContext;
import com.masterlearning.platform.modules.ai.dto.response.UnderstandingEvaluation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdaptiveTeachingDecisionService {
    private final AdaptiveExplanationPlanService explanationPlan;
    private final AdaptiveFollowUpRecommendationService followUp;
    private final TargetedPracticeRecommendationService practice;
    private final LearningStateInterventionService intervention;

    public AdaptiveTeachingDecisionService(AdaptiveExplanationPlanService explanationPlan,
                                           AdaptiveFollowUpRecommendationService followUp,
                                           TargetedPracticeRecommendationService practice,
                                           LearningStateInterventionService intervention) {
        this.explanationPlan = explanationPlan;
        this.followUp = followUp;
        this.practice = practice;
        this.intervention = intervention;
    }

    public AdaptiveTeachingDecision decide(LearnerTutorContext learner,
                                           TutorIntelligenceContext intelligence,
                                           UnderstandingEvaluation understanding) {
        AdaptiveExplanationPlan explanation = explanationPlan.plan(learner, intelligence);
        TargetedPracticeRecommendation practiceRecommendation = practice.recommend(learner, intelligence);
        LearningStateIntervention stateIntervention = intervention.decide(
                learner, intelligence, explanation, practiceRecommendation, understanding);
        AdaptiveFollowUpRecommendation followUpRecommendation = followUp.recommend(
                learner, intelligence, understanding);

        List<String> reasons = new ArrayList<>(stateIntervention.reasons());
        reasons.add("adaptive_explanation:" + explanation.level());
        reasons.add("follow_up:" + followUpRecommendation.mode());
        if (intelligence.signals().contains("prerequisite_blocker")) {
            reasons.add("prerequisite_blocker");
        }

        return new AdaptiveTeachingDecision(
                stateIntervention.intervention(),
                stateIntervention.teachingStrategy(),
                explanation.level(),
                followUpRecommendation.difficulty(),
                followUpRecommendation.mode(),
                stateIntervention.practiceRecommended(),
                intelligence.signals().contains("prerequisite_blocker"),
                List.copyOf(reasons)
        );
    }
}
