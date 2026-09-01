package com.masterlearning.platform.modules.assessment.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.assessment.dto.request.SubmitAssessmentRequest;
import com.masterlearning.platform.modules.assessment.dto.response.AssessmentResultResponse;
import com.masterlearning.platform.modules.assessment.entity.Question;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAttemptRepository;
import com.masterlearning.platform.modules.assessment.repository.AssessmentRepository;
import com.masterlearning.platform.modules.assessment.repository.QuestionOptionRepository;
import com.masterlearning.platform.modules.assessment.repository.QuestionRepository;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/assessments")
public class StudentAssessmentController {

    private final AssessmentRepository assessments;
    private final QuestionRepository questions;
    private final QuestionOptionRepository options;
    private final AssessmentAttemptRepository attempts;
    private final UserRepository users;

    public StudentAssessmentController(
            AssessmentRepository assessments,
            QuestionRepository questions,
            QuestionOptionRepository options,
            AssessmentAttemptRepository attempts,
            UserRepository users
    ) {
        this.assessments = assessments;
        this.questions = questions;
        this.options = options;
        this.attempts = attempts;
        this.users = users;
    }

    @PostMapping("/{assessmentId}/submit")
    public ApiResponse<AssessmentResultResponse> submit(
            @PathVariable UUID assessmentId,
            @Valid @RequestBody SubmitAssessmentRequest request
    ) {
        var assessment = assessments.findById(assessmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assessment not found"));

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        var user = users.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));

        var assessmentQuestions = questions.findByAssessmentId(assessmentId);
        if (assessmentQuestions.isEmpty()) {
            throw new IllegalArgumentException("Assessment has no questions");
        }

        int totalPoints = assessmentQuestions.stream()
                .mapToInt(Question::getPoints)
                .sum();

        int earnedPoints = 0;
        int correctAnswers = 0;

        for (var question : assessmentQuestions) {
            UUID selectedOptionId = request.answers().get(question.getId());

            boolean correct = selectedOptionId != null
                    && options.findByQuestionId(question.getId()).stream()
                    .anyMatch(option ->
                            option.getId().equals(selectedOptionId)
                                    && option.isCorrect());

            if (correct) {
                earnedPoints += question.getPoints();
                correctAnswers++;
            }
        }

        int score = (int) Math.round(
                earnedPoints * 100.0 / totalPoints
        );

        boolean passed = score >= assessment.getPassingScore();

        var attempt = attempts.save(
                new com.masterlearning.platform.modules.assessment.entity.AssessmentAttempt(
                        assessment,
                        user,
                        score,
                        passed
                )
        );

        return ApiResponse.success(
                "Assessment submitted",
                new AssessmentResultResponse(
                        attempt.getId(),
                        score,
                        passed,
                        correctAnswers,
                        assessmentQuestions.size()
                )
        );
    }
}
