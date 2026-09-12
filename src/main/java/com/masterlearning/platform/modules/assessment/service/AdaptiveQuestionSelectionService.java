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
        return select(assessment, userId, Set.of());
    }

    public AdaptiveAssessmentDecision select(Assessment assessment, UUID userId, Set<UUID> excludedQuestionIds) {
        List<Question> pool = questions.findByAssessmentId(assessment.getId()).stream()
                .filter(q -> excludedQuestionIds == null || !excludedQuestionIds.contains(q.getId()))
                .toList();
        if (pool.isEmpty()) throw new jakarta.persistence.EntityNotFoundException("No unanswered questions remain");

        Map<UUID, AdaptiveAssessmentRepository.QuestionSignal> history = signals.findQuestionSignals(assessment.getId(), userId)
                .stream().collect(Collectors.toMap(AdaptiveAssessmentRepository.QuestionSignal::questionId, Function.identity()));
        Map<UUID, Double> conceptMastery = graph.findLearnerMastery(assessment.getCourse().getId(), userId).stream()
                .collect(Collectors.toMap(LearningKnowledgeGraphRepository.MasteryRow::id, LearningKnowledgeGraphRepository.MasteryRow::mastery));

        Comparator<Question> ranking = Comparator.comparingDouble((Question q) -> score(q, history.get(q.getId()), conceptMastery))
                .thenComparing(Question::getId);
        Question selected = pool.stream().min(ranking).orElse(pool.getFirst());
        var signal = history.get(selected.getId());
        double mastery = conceptMastery.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double success = signal == null ? 0.0 : signal.successRate();
        String difficulty = success < 0.40 ? "EASY" : success >= 0.80 ? "HARD" : mastery < 50 ? "EASY" : mastery >= 85 ? "HARD" : "MEDIUM";
        String action = mastery < 50 || success < 0.40 ? "RETEACH_AND_ASSESS" : mastery >= 85 && success >= 0.80 ? "CHALLENGE" : "ASSESS";
        List<String> reasons = new ArrayList<>();
        if (signal == null || !signal.answeredByLearner()) reasons.add("Prefer unseen question");
        if (signal != null && signal.successRate() < 0.40 && signal.learnerAttempts() > 0) reasons.add("Question shows repeated learner difficulty");
        if (mastery < 60) reasons.add("Learner has weak concept mastery");
        if (!excludedQuestionIds.isEmpty()) reasons.add("Session excludes already answered questions");
        if (reasons.isEmpty()) reasons.add("Balanced adaptive selection");
        return new AdaptiveAssessmentDecision(assessment.getId(), selected.getId(), action, difficulty,
                signal != null && signal.conceptId() != null ? signal.conceptId().toString() : "ASSESSMENT_POOL",
                mastery, List.copyOf(reasons));
    }

    private double score(Question q, AdaptiveAssessmentRepository.QuestionSignal signal, Map<UUID, Double> conceptMastery) {
        if (signal == null || !signal.answeredByLearner()) return -100.0;
        double concept = signal.conceptId() == null ? 50.0 : conceptMastery.getOrDefault(signal.conceptId(), 0.0);
        double difficultyFit = Math.abs((100.0 - signal.successRate() * 100.0) - (100.0 - concept));
        return signal.learnerAttempts() * 10.0 + difficultyFit + (signal.everCorrect() ? 5.0 : 0.0);
    }
}
