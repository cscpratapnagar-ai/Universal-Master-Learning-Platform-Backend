package com.masterlearning.platform.modules.assessment.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAnswerRepository;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAttemptRepository;
import com.masterlearning.platform.modules.assessment.repository.AssessmentEvaluatorAssignmentRepository;
import com.masterlearning.platform.modules.assessment.repository.AssessmentRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/evaluator/assignments")
public class EvaluatorAttemptReviewController {
    private final AssessmentEvaluatorAssignmentRepository assignments;
    private final AssessmentAttemptRepository attempts;
    private final AssessmentAnswerRepository answers;
    private final AssessmentRepository assessments;

    public EvaluatorAttemptReviewController(AssessmentEvaluatorAssignmentRepository assignments,
                                            AssessmentAttemptRepository attempts,
                                            AssessmentAnswerRepository answers,
                                            AssessmentRepository assessments) {
        this.assignments = assignments;
        this.attempts = attempts;
        this.answers = answers;
        this.assessments = assessments;
    }

    @GetMapping("/{assessmentId}/attempts")
    @PreAuthorize("hasRole('EVALUATOR') or hasAuthority('ASSESSMENT:REVIEW')")
    @Transactional(readOnly = true)
    public ApiResponse<List<Map<String, Object>>> submittedAttempts(@PathVariable UUID assessmentId) {
        UUID evaluatorId = SecurityUtils.getCurrentUserId();
        if (!assessments.existsById(assessmentId)) throw new EntityNotFoundException("Assessment not found");
        requireAssignment(evaluatorId, assessmentId);

        List<Map<String, Object>> result = attempts.findByAssessmentIdOrderBySubmittedAtDesc(assessmentId)
                .stream().map(attempt -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("attemptId", attempt.getId());
                    item.put("learnerId", attempt.getUser().getId());
                    item.put("attemptNumber", attempt.getAttemptNumber());
                    item.put("score", attempt.getScore());
                    item.put("passed", attempt.isPassed());
                    item.put("masteryLevel", attempt.getMasteryLevel());
                    item.put("submittedAt", attempt.getSubmittedAt());
                    return item;
                }).toList();
        return ApiResponse.success("Submitted assessment attempts retrieved", result);
    }

    @GetMapping("/{assessmentId}/attempts/{attemptId}")
    @PreAuthorize("hasRole('EVALUATOR') or hasAuthority('ASSESSMENT:REVIEW')")
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> attemptDetails(@PathVariable UUID assessmentId,
                                                           @PathVariable UUID attemptId) {
        UUID evaluatorId = SecurityUtils.getCurrentUserId();
        if (!assessments.existsById(assessmentId)) throw new EntityNotFoundException("Assessment not found");
        requireAssignment(evaluatorId, assessmentId);
        var attempt = attempts.findById(attemptId)
                .filter(a -> a.getAssessment().getId().equals(assessmentId))
                .orElseThrow(() -> new EntityNotFoundException("Attempt not found for this assessment"));

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("attemptId", attempt.getId());
        item.put("assessmentId", assessmentId);
        item.put("assessmentTitle", attempt.getAssessment().getTitle());
        item.put("learnerId", attempt.getUser().getId());
        item.put("attemptNumber", attempt.getAttemptNumber());
        item.put("score", attempt.getScore());
        item.put("passed", attempt.isPassed());
        item.put("masteryLevel", attempt.getMasteryLevel());
        item.put("submittedAt", attempt.getSubmittedAt());
        item.put("answers", answers.findByAttemptId(attemptId).stream().map(answer -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("answerId", answer.getId());
            row.put("questionId", answer.getQuestion().getId());
            row.put("questionText", answer.getQuestion().getQuestionText());
            row.put("questionType", answer.getQuestion().getQuestionType());
            row.put("maxPoints", answer.getQuestion().getPoints());
            row.put("selectedOptionId", answer.getSelectedOption() == null ? null : answer.getSelectedOption().getId());
            row.put("selectedOptionText", answer.getSelectedOption() == null ? null : answer.getSelectedOption().getOptionText());
            row.put("correct", answer.isCorrect());
            row.put("pointsAwarded", answer.getPointsAwarded());
            return row;
        }).toList());
        return ApiResponse.success("Assessment attempt details retrieved", item);
    }

    private void requireAssignment(UUID evaluatorId, UUID assessmentId) {
        if (!assignments.existsByAssessmentIdAndEvaluatorId(assessmentId, evaluatorId)) {
            throw new AccessDeniedException("You are not assigned to review this assessment");
        }
    }
}