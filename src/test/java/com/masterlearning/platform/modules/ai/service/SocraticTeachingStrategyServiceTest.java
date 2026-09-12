package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.LearnerTutorContext;
import com.masterlearning.platform.modules.ai.dto.response.SocraticTeachingPlan;
import com.masterlearning.platform.modules.ai.dto.response.TutorIntelligenceContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SocraticTeachingStrategyServiceTest {

    private final SocraticTeachingStrategyService service = new SocraticTeachingStrategyService();

    @Test
    void prerequisiteBlockerGetsFoundationBridge() {
        LearnerTutorContext learner = context(45.0);
        TutorIntelligenceContext intelligence = intelligence(List.of("prerequisite_blocker"), "PREREQUISITE_BRIDGE");

        SocraticTeachingPlan plan = service.plan(learner, intelligence, false);

        assertEquals("PREREQUISITE_BRIDGE", plan.strategy());
        assertEquals("FOUNDATION_CHECK", plan.questionType());
        assertFalse(plan.revealAnswer());
    }

    @Test
    void misconceptionGetsContrastingSocraticQuestion() {
        LearnerTutorContext learner = context(65.0);
        TutorIntelligenceContext intelligence = intelligence(List.of("possible_misconception"), "MISCONCEPTION_REPAIR");

        SocraticTeachingPlan plan = service.plan(learner, intelligence, false);

        assertEquals("MISCONCEPTION_REPAIR", plan.strategy());
        assertEquals("CONTRASTING_QUESTION", plan.questionType());
        assertEquals(2, plan.guidanceLevel());
    }

    @Test
    void advancedLearnerGetsTransferChallenge() {
        LearnerTutorContext learner = context(92.0);
        TutorIntelligenceContext intelligence = intelligence(List.of(), "DEEPEN_AND_CHALLENGE");

        SocraticTeachingPlan plan = service.plan(learner, intelligence, false);

        assertEquals("DEEPEN_AND_CHALLENGE", plan.strategy());
        assertEquals("TRANSFER_QUESTION", plan.questionType());
        assertEquals(3, plan.guidanceLevel());
        assertFalse(plan.revealAnswer());
    }

    private LearnerTutorContext context(double mastery) {
        return new LearnerTutorContext(null, mastery, "ACTIVE", "LOW", "STABLE", List.of(), List.of(), "LEARN", "STANDARD");
    }

    private TutorIntelligenceContext intelligence(List<String> signals, String strategy) {
        return new TutorIntelligenceContext("NONE_DETECTED", strategy, "CHECK_UNDERSTANDING", "INTERMEDIATE", true, signals);
    }
}
