package com.masterlearning.platform.modules.assessment.service;

import com.masterlearning.platform.modules.assessment.dto.response.AdaptiveAssessmentDecision;
import com.masterlearning.platform.modules.assessment.entity.Assessment;
import com.masterlearning.platform.modules.assessment.entity.Question;
import com.masterlearning.platform.modules.assessment.repository.QuestionRepository;
import com.masterlearning.platform.modules.ai.repository.LearningKnowledgeGraphRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdaptiveAssessmentService {
    private final QuestionRepository questions;
    private final LearningKnowledgeGraphRepository graph;

    public AdaptiveAssessmentService(QuestionRepository questions, LearningKnowledgeGraphRepository graph) {
        this.questions = questions;
        this.graph = graph;
    }

    public AdaptiveAssessmentDecision nextQuestion(Assessment assessment, UUID userId) {
        List<Question> candidates = questions.findByAssessmentId(assessment.getId());
        if (candidates.isEmpty()) throw new EntityNotFoundException("Assessment has no questions");
        UUID courseId = assessment.getCourse().getId();
        Map<UUID, LearningKnowledgeGraphRepository.MasteryRow> mastery = graph.findLearnerMastery(courseId, userId).stream()
                .collect(Collectors.toMap(LearningKnowledgeGraphRepository.MasteryRow::id, x -> x));
        double average = mastery.values().stream().mapToDouble(LearningKnowledgeGraphRepository.MasteryRow::mastery).average().orElse(0.0);
        Question selected = candidates.stream().min(Comparator.comparingInt(q -> score(q, average))).orElse(candidates.getFirst());
        String difficulty = average < 50 ? "EASY" : average >= 85 ? "HARD" : "MEDIUM";
        String action = average < 50 ? "SCAFFOLD" : average >= 85 ? "CHALLENGE" : "ASSESS";
        List<String> reasons = new ArrayList<>();
        reasons.add("Learner mastery=" + Math.round(average));
        reasons.add("Question selected deterministically from the assessment pool");
        return new AdaptiveAssessmentDecision(assessment.getId(), selected.getId(), action, difficulty,
                "COURSE_MASTERY", average, List.copyOf(reasons));
    }

    private int score(Question q, double mastery) {
        int base = Math.abs(q.getPoints() - 1);
        if (mastery < 50) return base;
        if (mastery >= 85) return base + 1;
        return base;
    }
}
