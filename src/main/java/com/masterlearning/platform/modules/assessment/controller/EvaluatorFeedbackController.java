package com.masterlearning.platform.modules.assessment.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.assessment.entity.AssessmentAttemptReview;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAttemptRepository;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAttemptReviewRepository;
import com.masterlearning.platform.modules.assessment.repository.AssessmentEvaluatorAssignmentRepository;
import com.masterlearning.platform.modules.assessment.repository.AssessmentRepository;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/evaluator/assignments")
public class EvaluatorFeedbackController {
    private final AssessmentEvaluatorAssignmentRepository assignments;
    private final AssessmentAttemptRepository attempts;
    private final AssessmentAttemptReviewRepository reviews;
    private final AssessmentRepository assessments;
    private final UserRepository users;

    public EvaluatorFeedbackController(AssessmentEvaluatorAssignmentRepository assignments,
                                       AssessmentAttemptRepository attempts,
                                       AssessmentAttemptReviewRepository reviews,
                                       AssessmentRepository assessments,
                                       UserRepository users) {
        this.assignments = assignments;
        this.attempts = attempts;
        this.reviews = reviews;
        this.assessments = assessments;
        this.users = users;
    }

    @PutMapping("/{assessmentId}/attempts/{attemptId}/review")
    @PreAuthorize("hasRole('EVALUATOR') or hasAuthority('ASSESSMENT:REVIEW')")
    @Transactional
    public ApiResponse<Map<String, Object>> saveFeedback(@PathVariable UUID assessmentId,
                                                         @PathVariable UUID attemptId,
                                                         @Valid @RequestBody SaveFeedbackRequest request) {
        UUID reviewerId = SecurityUtils.getCurrentUserId();
        if (!assessments.existsById(assessmentId)) {
            throw new EntityNotFoundException("Assessment not found");
        }
        if (!assignments.existsByAssessmentIdAndEvaluatorId(assessmentId, reviewerId)) {
            throw new AccessDeniedException("You are not assigned to review this assessment");
        }
        var attempt = attempts.findById(attemptId)
                .filter(a -> a.getAssessment().getId().equals(assessmentId))
                .orElseThrow(() -> new EntityNotFoundException("Attempt not found for this assessment"));
        var reviewer = users.findById(reviewerId)
                .orElseThrow(() -> new EntityNotFoundException("Current evaluator not found"));

        var review = reviews.findByAttemptId(attemptId).orElse(null);
        if (review == null) {
            review = reviews.save(new AssessmentAttemptReview(attempt, reviewer, request.remarks()));
        } else {
            review.updateRemarks(reviewer, request.remarks());
            review = reviews.save(review);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("reviewId", review.getId());
        response.put("attemptId", attemptId);
        response.put("assessmentId", assessmentId);
        response.put("reviewerId", reviewerId);
        response.put("remarks", review.getRemarks());
        response.put("reviewedAt", review.getReviewedAt());
        response.put("scoreUnchanged", true);
        return ApiResponse.success("Evaluator feedback saved; automatic score was not changed", response);
    }

    @GetMapping("/{assessmentId}/attempts/{attemptId}/review")
    @PreAuthorize("hasRole('EVALUATOR') or hasAuthority('ASSESSMENT:REVIEW')")
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> getFeedback(@PathVariable UUID assessmentId,
                                                        @PathVariable UUID attemptId) {
        UUID evaluatorId = SecurityUtils.getCurrentUserId();
        if (!assessments.existsById(assessmentId)) {
            throw new EntityNotFoundException("Assessment not found");
        }
        if (!assignments.existsByAssessmentIdAndEvaluatorId(assessmentId, evaluatorId)) {
            throw new AccessDeniedException("You are not assigned to review this assessment");
        }
        var attempt = attempts.findById(attemptId)
                .filter(a -> a.getAssessment().getId().equals(assessmentId))
                .orElseThrow(() -> new EntityNotFoundException("Attempt not found for this assessment"));
        var review = reviews.findByAttemptId(attempt.getId())
                .orElseThrow(() -> new EntityNotFoundException("No evaluator feedback exists for this attempt"));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("reviewId", review.getId());
        response.put("attemptId", attemptId);
        response.put("reviewerId", review.getReviewer().getId());
        response.put("remarks", review.getRemarks());
        response.put("reviewedAt", review.getReviewedAt());
        return ApiResponse.success("Evaluator feedback retrieved", response);
    }

    public record SaveFeedbackRequest(@NotBlank @Size(max = 5000) String remarks) {}
}