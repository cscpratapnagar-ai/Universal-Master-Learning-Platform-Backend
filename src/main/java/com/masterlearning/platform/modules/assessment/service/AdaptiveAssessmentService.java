package com.masterlearning.platform.modules.assessment.service;

import com.masterlearning.platform.modules.assessment.dto.response.AdaptiveAssessmentDecision;
import com.masterlearning.platform.modules.assessment.entity.Assessment;
import com.masterlearning.platform.modules.assessment.entity.Question;
import com.masterlearning.platform.modules.assessment.repository.AdaptiveAssessmentRepository;
import com.masterlearning.platform.modules.assessment.repository.QuestionRepository;
import com.masterlearning.platform.modules.ai.repository.LearningKnowledgeGraphRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdaptiveAssessmentService {
    private final QuestionRepository questions;
    private final AdaptiveAssessmentRepository signals;
    private final LearningKnowledgeGraphRepository graph;

    public AdaptiveAssessmentService(QuestionRepository questions, AdaptiveAssessmentRepository signals,
                                     LearningKnowledgeGraphRepository graph) {
        this.questions = questions;
        this.signals = signals;
        this.graph = graph;
    }

    public AdaptiveAssessmentDecision nextQuestion(Assessment assessment, UUID userId) {
        List<Question> candidates = questions.findByAssessmentId(assessment.getId());
        if (candidates.isEmpty()) throw new EntityNotFoundException("Assessment has no questions");
        UUID courseId = assessment.getCourse().getId();
        Map<UUID, LearningKnowledgeGraphRepository.MasteryRow> mastery = graph.findLearnerMastery(courseId, userId).stream()
                .collect(Collectors.toMap(LearningKnowledgeGraphRepository.MasteryRow::id, x -> x));
        double average = mastery.values().stream().mapToDouble(LearningKnowledgeGraphRepository.MasteryRow::mastery).average().orElse(0.0);
        Map<UUID, AdaptiveAssessmentRepository.QuestionSignal> history = signals.findQuestionSignals(assessment.getId(), userId).stream()
                .collect(Collectors.toMap(AdaptiveAssessmentRepository.QuestionSignal::questionId, x -> x));
        Set<UUID> weakConcepts = mastery.entrySet().stream().filter(e -> e.getValue().mastery() < 60.0)
                .map(Map.Entry::getKey).collect(Collectors.toSet());
        String targetDifficulty = average < 50.0 ? "EASY" : average >= 85.0 ? "HARD" : "MEDIUM";
        Question selected = candidates.stream()
                .min(Comparator.comparingInt(q -> score(q, history.get(q.getId()), targetDifficulty, weakConcepts)))
                .orElse(candidates.getFirst());
        AdaptiveAssessmentRepository.QuestionSignal signal = history.get(selected.getId());
        String action = average < 50.0 ? "SCAFFOLD" : average >= 85.0 ? "CHALLENGE" : "ASSESS";
        List<String> reasons = new ArrayList<>();
        reasons.add("Learner mastery=" + Math.round(average));
        reasons.add("Target difficulty=" + targetDifficulty);
        if (signal == null || !signal.answeredByLearner()) reasons.add("Prioritize unseen question");
        else if (!signal.everCorrect()) reasons.add("Revisit previously unsuccessful question");
        else reasons.add("Avoid unnecessary repetition of mastered question");
        if (signal != null && signal.conceptId() != null && weakConcepts.contains(signal.conceptId()))
            reasons.add("Question targets a weak concept");
        reasons.add("Question history is scoped to the current learner");
        String focus = signal != null && signal.conceptId() != null && weakConcepts.contains(signal.conceptId())
                ? "WEAK_CONCEPT" : "ADAPTIVE_ASSESSMENT";
        return new AdaptiveAssessmentDecision(assessment.getId(), selected.getId(), action, targetDifficulty,
                focus, average, List.copyOf(reasons));
    }

    private int score(Question q, AdaptiveAssessmentRepository.QuestionSignal signal,
                      String targetDifficulty, Set<UUID> weakConcepts) {
        int score = q.getDifficultyLevel().equalsIgnoreCase(targetDifficulty) ? 0 : 30;
        if (signal != null && signal.answeredByLearner()) score += 20;
        if (signal != null && !signal.everCorrect()) score -= 10;
        if (signal != null && signal.conceptId() != null && weakConcepts.contains(signal.conceptId())) score -= 25;
        if (signal != null) score += (int) Math.min(signal.learnerAttempts(), 5);
        return score + Math.abs(q.getPoints() - 1);
    }
}
