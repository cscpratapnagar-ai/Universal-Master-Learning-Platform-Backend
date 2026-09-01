package com.masterlearning.platform.modules.assessment.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.assessment.dto.request.SubmitAssessmentRequest;
import com.masterlearning.platform.modules.assessment.dto.response.AssessmentResultResponse;
import com.masterlearning.platform.modules.assessment.entity.AssessmentAnswer;
import com.masterlearning.platform.modules.assessment.entity.Question;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAnswerRepository;
import com.masterlearning.platform.modules.assessment.repository.AssessmentAttemptRepository;
import com.masterlearning.platform.modules.assessment.repository.AssessmentRepository;
import com.masterlearning.platform.modules.assessment.repository.QuestionOptionRepository;
import com.masterlearning.platform.modules.assessment.repository.QuestionRepository;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student/assessments")
public class StudentAssessmentController {
    private final AssessmentRepository assessments;
    private final QuestionRepository questions;
    private final QuestionOptionRepository options;
    private final AssessmentAttemptRepository attempts;
    private final AssessmentAnswerRepository answers;
    private final UserRepository users;

    public StudentAssessmentController(AssessmentRepository assessments, QuestionRepository questions,
                                       QuestionOptionRepository options, AssessmentAttemptRepository attempts,
                                       AssessmentAnswerRepository answers, UserRepository users) {
        this.assessments=assessments; this.questions=questions; this.options=options;
        this.attempts=attempts; this.answers=answers; this.users=users;
    }

    @PostMapping("/{assessmentId}/submit")
    @Transactional
    public ApiResponse<AssessmentResultResponse> submit(@PathVariable UUID assessmentId,
                                                          @Valid @RequestBody SubmitAssessmentRequest request) {
        var assessment=assessments.findById(assessmentId).orElseThrow(()->new EntityNotFoundException("Assessment not found"));
        UUID currentUserId=SecurityUtils.getCurrentUserId();
        var user=users.findById(currentUserId).orElseThrow(()->new EntityNotFoundException("Current user not found"));

        long previousAttempts=attempts.countByAssessmentIdAndUserId(assessmentId,currentUserId);
        if(previousAttempts>=assessment.getMaxAttempts()) throw new IllegalStateException("Maximum assessment attempts reached");

        var assessmentQuestions=questions.findByAssessmentId(assessmentId);
        if(assessmentQuestions.isEmpty()) throw new IllegalArgumentException("Assessment has no questions");

        int totalPoints=assessmentQuestions.stream().mapToInt(Question::getPoints).sum();
        int earnedPoints=0, correctAnswers=0;
        for(var question:assessmentQuestions){
            UUID selectedId=request.answers().get(question.getId());
            boolean correct=false;
            if(selectedId!=null){
                var selected=options.findById(selectedId).orElseThrow(()->new IllegalArgumentException("Selected option not found"));
                boolean belongs=options.findByQuestionId(question.getId()).stream().anyMatch(o->o.getId().equals(selectedId));
                if(!belongs) throw new IllegalArgumentException("Selected option does not belong to the question");
                correct=selected.isCorrect();
            }
            if(correct){earnedPoints+=question.getPoints(); correctAnswers++;}
        }

        int score=(int)Math.round(earnedPoints*100.0/totalPoints);
        boolean passed=score>=assessment.getPassingScore();
        String masteryLevel=masteryLevel(score);
        var attempt=attempts.save(new com.masterlearning.platform.modules.assessment.entity.AssessmentAttempt(
                assessment,user,(int)previousAttempts+1,score,passed,masteryLevel));

        for(var question:assessmentQuestions){
            UUID selectedId=request.answers().get(question.getId());
            var selected=selectedId==null?null:options.findById(selectedId).orElse(null);
            boolean correct=selected!=null && selected.isCorrect();
            answers.save(new AssessmentAnswer(attempt,question,selected,correct,correct?question.getPoints():0));
        }

        return ApiResponse.success("Assessment submitted",new AssessmentResultResponse(
                attempt.getId(),score,passed,correctAnswers,assessmentQuestions.size()));
    }

    private String masteryLevel(int score){
        if(score>=90) return "MASTERED";
        if(score>=70) return "PROFICIENT";
        if(score>=50) return "DEVELOPING";
        return "NEEDS_REVIEW";
    }
}