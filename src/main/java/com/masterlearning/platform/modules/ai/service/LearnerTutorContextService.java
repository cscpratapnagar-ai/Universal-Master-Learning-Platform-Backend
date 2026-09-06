package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.LearnerTutorContext;
import com.masterlearning.platform.modules.learning.engine.LearnerProfileEngine;
import com.masterlearning.platform.modules.learning.dto.response.LearnerProfileResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LearnerTutorContextService {
    private final LearnerProfileEngine profileEngine;

    public LearnerTutorContextService(LearnerProfileEngine profileEngine) {
        this.profileEngine = profileEngine;
    }

    public LearnerTutorContext build(UUID enrollmentId) {
        LearnerProfileResponse profile = profileEngine.build(enrollmentId);
        double mastery = profile.masteryScore();
        String style = mastery < 50 ? "SIMPLE_STEP_BY_STEP"
                : mastery >= 85 ? "CHALLENGE_AND_ACCELERATE"
                : "BALANCED_EXPLANATION";
        String focus = profile.focusArea() == null || profile.focusArea().isBlank()
                ? "No specific focus area identified"
                : profile.focusArea();
        return new LearnerTutorContext(
                enrollmentId,
                mastery,
                profile.learnerState(),
                profile.riskLevel(),
                profile.momentum(),
                List.of(focus),
                profile.recommendedNextAction(),
                style
        );
    }
}
