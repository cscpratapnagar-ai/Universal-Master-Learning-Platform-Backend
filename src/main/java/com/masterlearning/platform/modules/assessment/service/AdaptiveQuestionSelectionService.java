package com.masterlearning.platform.modules.assessment.service;

import com.masterlearning.platform.modules.assessment.dto.response.AdaptiveAssessmentDecision;
import com.masterlearning.platform.modules.assessment.entity.Assessment;
import com.masterlearning.platform.modules.assessment.entity.Question;
import com.masterlearning.platform.modules.assessment.repository.AdaptiveAssessmentRepository;
import com.masterlearning.platform.modules.assessment.repository.QuestionRepository;
import com.masterlearning.platform.modules.ai.repository.LearningKnowledgeGraphRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdaptiveQuestionSelectionService {
    private final QuestionRepository questions;
    private final AdaptiveAssessmentRepository signals;
    private final LearningKnowledgeGraphRepository graph;

    public AdaptiveQuestionSelectionService(QuestionRepository questions,
                                            AdaptiveAssessmentRepository signals,
                                            LearningKnowledgeGraphRepository graph) {
        this.questions = questions;
        this.signals = signals;
        this.graph = graph;
    }

    public AdaptiveAssessmentDecision select(Assessment assessment, UUID userId) {
        return select(assessment, userId, Set.of(), 0, 0);
    }

    public AdaptiveAssessmentDecision select(Assessment assessment, UUID userId, Set<UUID> excludedQuestionIds) {
        return select(assessment, userId, excludedQuestionIds, 0, 0);
    }

    public AdaptiveAssessmentDecision select(Assessment assessment, UUID userId, Set<UUID> excludedQuestionIds,
                                             int recentCorrectStreak, int recentIncorrectStreak) {
        Set<UUID> excluded = excludedQuestionIds == null ? Set.of() : excludedQuestionIds;
        List<Question> pool = questions.findByAssessmentId(assessment.getId()).stream()
                .filter(q -> !excluded.contains(q.getId()))
                .toList();
        if (pool.isEmpty()) throw new jakarta.persistence.EntityNotFoundException("No unanswered questions remain");

        Map<UUID, AdaptiveAssessmentRepository.QuestionSignal> history = signals.findQuestionSignals(assessment.getId(), userId)
                .stream().collect(Collectors.toMap(AdaptiveAssessmentRepository.QuestionSignal::questionId, Function.identity()));
        Map<UUID, Double> conceptMastery = graph.findLearnerMastery(assessment.getCourse().getId(), userId).stream()
                .collect(Collectors.toMap(LearningKnowledgeGraphRepository.MasteryRow::id, LearningKnowledgeGraphRepository.MasteryRow::mastery));

        Comparator<Question> ranking = Comparator.comparingDouble((Question q) -> score(q, history.get(q.getId()), conceptMastery,
                        recentCorrectStreak, recentIncorrectStreak))
                .thenComparing(Question::getId);
        Question selected = pool.stream().min(ranking).orElse(pool.getFirst());
        var signal = history.get(selected.getId());
        double mastery = conceptMastery.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double success = signal == null ? 0.0 : signal.successRate();

        String difficulty;
        String action;
        if (recentIncorrectStreak >= 2) {
            difficulty = "EASY";
            action = "RETEACH_AND_ASSESS";
        } else if (recentCorrectStreak >= 2 && mastery >= 70.0) {
            difficulty = "HARD";
            action = "CHALLENGE";
        } else {
            difficulty = success < 0.40 ? "EASY" : success >= 0.80 ? "HARD" : mastery < 50 ? "EASY" : mastery >= 85 ? "HARD" : "MEDIUM";
            action = mastery < 50 || success < 0.40 ? "RETEACH_AND_ASSESS" : mastery >= 85 && success >= 0.80 ? "CHALLENGE" : "ASSESS";
        }

        List<String> reasons = new ArrayList<>();
        if (signal == null || !signal.answeredByLearner()) reasons.add("Prefer unseen question");
        if (signal != null && signal.successRate() < 0.40 && signal.learnerAttempts() > 0) reasons.add("Question shows repeated learner difficulty");
        if (mastery < 60) reasons.add("Learner has weak concept mastery");
        if (recentIncorrectStreak >= 2) reasons.add("Recent incorrect streak triggers scaffolding");
        if (recentCorrectStreak >= 2 && mastery >= 70.0) reasons.add("Recent success streak supports increased challenge");
        if (!excluded.isEmpty()) reasons.add("Session excludes already answered questions");
        if (reasons.isEmpty()) reasons.add("Balanced adaptive selection");

        return new AdaptiveAssessmentDecision(assessment.getId(), selected.getId(), action, difficulty,
                signal != null && signal.conceptId() != null ? signal.conceptId().toString() : "ASSESSMENT_POOL",
                mastery, List.copyOf(reasons));
    }

    private double score(Question q, AdaptiveAssessmentRepository.QuestionSignal signal, Map<UUID, Double> conceptMastery,
                         int recentCorrectStreak, int recentIncorrectStreak) {
        if (signal == null || !signal.answeredByLearner()) return -100.0;
        double concept = signal.conceptId() == null ? 50.0 : conceptMastery.getOrDefault(signal.conceptId(), 0.0);
        double difficultyFit = Math.abs((100.0 - signal.successRate() * 100.0) - (100.0 - concept));
        double streakAdjustment = recentIncorrectStreak >= 2 && concept < 70.0 ? -15.0
                : recentCorrectStreak >= 2 && concept >= 70.0 ? 8.0 : 0.0;
        return signal.learnerAttempts() * 10.0 + difficultyFit + (signal.everCorrect() ? 5.0 : 0.0) + streakAdjustment;
    }
}
