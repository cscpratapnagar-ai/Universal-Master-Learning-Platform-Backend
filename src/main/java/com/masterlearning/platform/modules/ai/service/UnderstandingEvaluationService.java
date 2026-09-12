package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.UnderstandingEvaluation;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UnderstandingEvaluationService {

    public UnderstandingEvaluation evaluate(String learnerResponse) {
        String value = learnerResponse == null ? "" : learnerResponse.trim().toLowerCase(Locale.ROOT);
        if (value.isBlank()) {
            return new UnderstandingEvaluation("UNKNOWN", "ASK_AGAIN", true, false);
        }

        boolean confusion = containsAny(value, "don't understand", "dont understand", "not understand", "confused", "still stuck", "i'm stuck", "im stuck", "no idea");
        boolean uncertainty = containsAny(value, "maybe", "i think", "not sure", "probably", "i guess", "is it", "right?");
        boolean positive = containsAny(value, "understand", "got it", "makes sense", "i get it", "clear", "yes", "correct");
        boolean practice = containsAny(value, "example", "practice", "question", "quiz", "test me", "try one");

        if (confusion) return new UnderstandingEvaluation("CONFUSED", "RETEACH_WITH_SCAFFOLDING", true, true);
        if (uncertainty) return new UnderstandingEvaluation("UNCERTAIN", "CHECK_WITH_TARGETED_QUESTION", true, true);
        if (practice) return new UnderstandingEvaluation("ENGAGED", "GIVE_TARGETED_PRACTICE", false, true);
        if (positive) return new UnderstandingEvaluation("LIKELY_UNDERSTOOD", "DEEPEN_OR_ADVANCE", false, false);
        return new UnderstandingEvaluation("PARTIAL_OR_UNKNOWN", "CHECK_WITH_TARGETED_QUESTION", false, true);
    }

    private boolean containsAny(String value, String... terms) {
        for (String term : terms) if (value.contains(term)) return true;
        return false;
    }
}
