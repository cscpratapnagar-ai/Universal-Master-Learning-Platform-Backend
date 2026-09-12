package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.LearnerTutorContext;
import com.masterlearning.platform.modules.ai.dto.response.SocraticTeachingPlan;
import com.masterlearning.platform.modules.ai.dto.response.TutorIntelligenceContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SocraticTeachingStrategyService {

    public SocraticTeachingPlan plan(LearnerTutorContext learner,
                                     TutorIntelligenceContext intelligence,
                                     boolean understandingRetry) {
        if (intelligence.signals().contains("prerequisite_blocker")) {
            return new SocraticTeachingPlan(
                    "PREREQUISITE_BRIDGE",
                    "GUIDED_FOUNDATION",
                    "FOUNDATION_CHECK",
                    1,
                    false,
                    List.of("Identify the missing prerequisite", "Ask one simple diagnostic question", "Explain only the missing foundation", "Re-check understanding")
            );
        }

        if (intelligence.signals().contains("possible_misconception")) {
            return new SocraticTeachingPlan(
                    "MISCONCEPTION_REPAIR",
                    "SOCRATIC",
                    "CONTRASTING_QUESTION",
                    2,
                    false,
                    List.of("Surface the learner's assumption", "Ask a contrasting question", "Use course-grounded evidence", "Ask the learner to restate the concept")
            );
        }

        if (understandingRetry || "CONFUSED".equals(intelligence.followUpMode())) {
            return new SocraticTeachingPlan(
                    "SCAFFOLDED_SOCRATIC",
                    "STEP_BY_STEP",
                    "GUIDING_QUESTION",
                    2,
                    false,
                    List.of("Break the concept into one step", "Ask a guiding question", "Give a small hint if needed", "Check the learner's reasoning")
            );
        }

        if (learner.masteryScore() >= 85.0) {
            return new SocraticTeachingPlan(
                    "DEEPEN_AND_CHALLENGE",
                    "SOCRATIC_CHALLENGE",
                    "TRANSFER_QUESTION",
                    3,
                    false,
                    List.of("Ask the learner to predict", "Challenge the reasoning", "Introduce a transfer scenario", "Request justification")
            );
        }

        return new SocraticTeachingPlan(
                "GUIDED_DISCOVERY",
                "SOCRATIC",
                "GUIDING_QUESTION",
                2,
                false,
                List.of("Ask what the learner already knows", "Guide toward the key idea", "Validate the reasoning", "Confirm understanding")
        );
    }
}
