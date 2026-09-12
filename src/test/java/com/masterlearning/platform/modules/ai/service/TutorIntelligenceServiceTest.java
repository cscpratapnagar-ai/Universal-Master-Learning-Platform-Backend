package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.LearnerTutorContext;
import com.masterlearning.platform.modules.ai.dto.response.TutorIntelligenceContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TutorIntelligenceServiceTest {
    private final TutorIntelligenceService service = new TutorIntelligenceService();

    private LearnerTutorContext learner(double mastery, List<String> weakConcepts) {
        return new LearnerTutorContext(UUID.randomUUID(), mastery, "LEARNING", "LOW", "STEADY", List.of(), weakConcepts, "LEARN", "CLEAR");
    }

    @Test
    void detectsPossibleMisconceptionAndRecommendsPractice() {
        TutorIntelligenceContext result = service.analyze("So it means that force is the same as mass, right?", learner(65, List.of("Force")), false);

        assertEquals("POSSIBLE", result.misconceptionSignal());
        assertEquals("MISCONCEPTION_REPAIR", result.teachingStrategy());
        assertTrue(result.practiceRecommended());
        assertTrue(result.signals().contains("possible_misconception"));
    }

    @Test
    void prerequisiteBlockerHasHighestTeachingPriority() {
        TutorIntelligenceContext result = service.analyze("Explain this concept", learner(80, List.of()), true);

        assertEquals("PREREQUISITE_BRIDGE", result.teachingStrategy());
        assertEquals("CHECK_UNDERSTANDING", result.followUpMode());
    }

    @Test
    void advancedLearnerGetsChallengeStrategy() {
        TutorIntelligenceContext result = service.analyze("Explain this in more depth", learner(92, List.of()), false);

        assertEquals("DEEPEN_AND_CHALLENGE", result.teachingStrategy());
        assertEquals("ADVANCED", result.explanationLevel());
    }
}
