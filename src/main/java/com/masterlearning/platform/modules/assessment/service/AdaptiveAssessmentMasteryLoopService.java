package com.masterlearning.platform.modules.assessment.service;

import com.masterlearning.platform.modules.assessment.dto.response.AdaptiveAssessmentMasteryLoop;
import com.masterlearning.platform.modules.assessment.entity.AssessmentAttempt;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAttemptRepository;
import com.masterlearning.platform.modules.ai.repository.LearningKnowledgeGraphRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdaptiveAssessmentMasteryLoopService {
    private final JdbcTemplate jdbc;
    private final AssessmentAttemptRepository attempts;
    private final LearningKnowledgeGraphRepository graph;

    public AdaptiveAssessmentMasteryLoopService(JdbcTemplate jdbc,
                                                AssessmentAttemptRepository attempts,
                                                LearningKnowledgeGraphRepository graph) {
        this.jdbc = jdbc;
        this.attempts = attempts;
        this.graph = graph;
    }

    public AdaptiveAssessmentMasteryLoop evaluate(UUID sessionId, UUID userId) {
        SessionRow session = jdbc.query("""
                SELECT assessment_id, user_id, status
                FROM assessment_sessions
                WHERE id = ? AND user_id = ?
                """, (rs, rowNum) -> new SessionRow(
                rs.getObject("assessment_id", UUID.class),
                rs.getObject("user_id", UUID.class),
                rs.getString("status")), sessionId, userId).stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Assessment session not found"));

        if (!"COMPLETED".equals(session.status())) {
            throw new IllegalStateException("Mastery loop is available only after session completion");
        }

        AssessmentAttempt attempt = attempts.findByAssessmentIdAndUserIdOrderByAttemptNumberDesc(
                session.assessmentId(), userId).stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Assessment attempt not found"));

        List<LearningKnowledgeGraphRepository.MasteryRow> masteryRows = graph.findLearnerMastery(
                attempt.getAssessment().getCourse().getId(), userId);
        double courseMastery = masteryRows.stream()
                .mapToDouble(LearningKnowledgeGraphRepository.MasteryRow::mastery)
                .average().orElse(0.0);
        int weakConcepts = (int) masteryRows.stream().filter(row -> row.mastery() < 60.0).count();

        String nextAction;
        if (attempt.getScore() < 50 || courseMastery < 50) {
            nextAction = "REMEDIATE";
        } else if (attempt.getScore() < 70 || weakConcepts > 0) {
            nextAction = "TARGETED_PRACTICE";
        } else if (attempt.getScore() >= 85 && courseMastery >= 85) {
            nextAction = "ADVANCE";
        } else {
            nextAction = "CONTINUE_LEARNING";
        }

        List<String> reasons = new ArrayList<>();
        reasons.add("Assessment result feeds learner mastery state");
        if (weakConcepts > 0) reasons.add("Weak concepts remain after assessment");
        if (attempt.getScore() >= 85 && courseMastery >= 85) reasons.add("Strong assessment and course mastery support advancement");
        if (reasons.size() == 1) reasons.add("Assessment performance supports continued learning");

        return new AdaptiveAssessmentMasteryLoop(
                sessionId,
                session.assessmentId(),
                attempt.getScore(),
                attempt.isPassed(),
                attempt.getMasteryLevel(),
                nextAction,
                Math.round(courseMastery * 100.0) / 100.0,
                weakConcepts,
                List.copyOf(reasons));
    }

    private record SessionRow(UUID assessmentId, UUID userId, String status) {}
}
