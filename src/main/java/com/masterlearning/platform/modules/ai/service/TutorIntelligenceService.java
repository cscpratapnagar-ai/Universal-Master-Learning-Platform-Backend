package com.masterlearning.platform.modules.ai.service;

import com.masterlearning.platform.modules.ai.dto.response.LearnerTutorContext;
import com.masterlearning.platform.modules.ai.dto.response.TutorIntelligenceContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class TutorIntelligenceService {

    public TutorIntelligenceContext analyze(String question, LearnerTutorContext learner, boolean prerequisiteBlocker) {
        String q = question == null ? "" : question.trim().toLowerCase(Locale.ROOT);
        List<String> signals = new ArrayList<>();

        boolean explicitConfusion = containsAny(q, "why", "how", "confused", "don't understand", "dont understand", "not understand", "wrong", "mistake", "stuck", "can't understand", "cant understand");
        boolean misconceptionPattern = containsAny(q, "so it means", "so that means", "therefore", "isn't it", "isnt it", "right?", "correct?", "means that");
        boolean practiceIntent = containsAny(q, "example", "practice", "exercise", "question", "quiz", "test me", "give me a problem");

        if (explicitConfusion) signals.add("explicit_confusion");
        if (misconceptionPattern) signals.add("possible_misconception");
        if (practiceIntent) signals.add("practice_intent");
        if (!learner.weakConcepts().isEmpty()) signals.add("weak_concept_context");
        if (prerequisiteBlocker) signals.add("prerequisite_blocker");

        String misconceptionSignal = misconceptionPattern || explicitConfusion ? "POSSIBLE" : "NONE_DETECTED";
        String strategy;
        if (prerequisiteBlocker) strategy = "PREREQUISITE_BRIDGE";
        else if (misconceptionPattern) strategy = "MISCONCEPTION_REPAIR";
        else if (explicitConfusion) strategy = "SOCRATIC_GUIDANCE";
        else if (learner.masteryScore() >= 85.0) strategy = "DEEPEN_AND_CHALLENGE";
        else if (learner.masteryScore() < 50.0) strategy = "SCAFFOLDED_EXPLANATION";
        else strategy = "DIRECT_TEACHING";

        String followUp = practiceIntent || learner.masteryScore() < 70.0 ? "CHECK_UNDERSTANDING" : "OPTIONAL_CHALLENGE";
        String explanationLevel = learner.masteryScore() < 50.0 ? "FOUNDATIONAL" : learner.masteryScore() >= 85.0 ? "ADVANCED" : "INTERMEDIATE";
        boolean practice = practiceIntent || learner.masteryScore() < 70.0 || misconceptionPattern;

        return new TutorIntelligenceContext(misconceptionSignal, strategy, followUp, explanationLevel, practice, List.copyOf(signals));
    }

    private boolean containsAny(String value, String... terms) {
        for (String term : terms) if (value.contains(term)) return true;
        return false;
    }
}
