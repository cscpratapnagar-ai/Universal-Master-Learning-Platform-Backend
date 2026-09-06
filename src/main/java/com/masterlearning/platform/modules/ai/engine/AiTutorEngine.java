package com.masterlearning.platform.modules.ai.engine;

import com.masterlearning.platform.modules.ai.dto.response.AiTutorResponse;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

@Component
public class AiTutorEngine {
    public AiTutorResponse respond(UUID enrollmentId, String message, double mastery, int progressPercent, int weakAreas) {
        String text = message == null ? "" : message.trim();
        String normalized = text.toLowerCase(Locale.ROOT);
        String intent;
        String response;
        String nextAction;
        String rationale;

        if (normalized.contains("hint") || normalized.contains("help")) {
            intent = "HINT";
            response = "Start with the core idea, identify what the question is asking, and work through one step at a time. Avoid jumping directly to the final answer.";
            nextAction = weakAreas > 0 ? "REVIEW_WEAK_AREA" : "CONTINUE_LEARNING";
            rationale = "A hint request should support learning without immediately revealing the answer.";
        } else if (normalized.contains("quiz") || normalized.contains("test") || normalized.contains("question")) {
            intent = "PRACTICE";
            response = "Practice mode is recommended. Begin with a short question on your current learning area and use the result as fresh evidence.";
            nextAction = "START_PRACTICE";
            rationale = "Practice creates new evidence that can improve personalization.";
        } else if (normalized.contains("explain") || normalized.contains("why") || normalized.contains("what is")) {
            intent = "EXPLAIN";
            response = "Use a concept-first explanation: definition, simple example, then a quick check for understanding. Your explanation depth should adapt to your current mastery.";
            nextAction = weakAreas > 0 ? "EXPLAIN_AND_REMEDIATE" : "EXPLAIN_AND_CHECK";
            rationale = "Explanation is prioritized when the learner asks for conceptual understanding.";
        } else {
            intent = "GUIDANCE";
            response = mastery < 50 ? "Your current signal suggests focusing on foundations before advancing. Review weak areas, then practice and reassess." : mastery >= 85 ? "Your mastery signal is strong. You can accelerate, attempt a harder practice task, or explore the next lesson." : "Continue with the next available learning activity and use practice results to refine your path.";
            nextAction = mastery < 50 ? "BUILD_MASTERY" : mastery >= 85 ? "ACCELERATE" : "CONTINUE_LEARNING";
            rationale = "Guidance is selected from the learner's current mastery and progress context.";
        }

        String learnerState = mastery < 50 ? "FOUNDATION" : mastery >= 85 ? "ADVANCED" : "DEVELOPING";
        if (progressPercent == 100) learnerState = "COURSE_COMPLETE";
        return new AiTutorResponse(enrollmentId, intent, learnerState, response, nextAction, rationale, true);
    }
}
