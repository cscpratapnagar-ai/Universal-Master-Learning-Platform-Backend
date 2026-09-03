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
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.modules.course.repository.LessonRepository;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final EnrollmentRepository enrollments;
    private final LessonRepository lessons;

    public StudentAssessmentController(AssessmentRepository assessments, QuestionRepository questions,
                                       QuestionOptionRepository options, AssessmentAttemptRepository attempts,
                                       AssessmentAnswerRepository answers, UserRepository users,
                                       EnrollmentRepository enrollments, LessonRepository lessons) {
        this.assessments=assessments; this.questions=questions; this.options=options;
        this.attempts=attempts; this.answers=answers; this.users=users;
        this.enrollments=enrollments; this.lessons=lessons;
    }

    @GetMapping("/lessons/{lessonId}")
    @Transactional(readOnly = true)
    public ApiResponse<List<Map<String,Object>>> forLesson(@PathVariable UUID lessonId) {
        var lesson = lessons.findById(lessonId).orElseThrow(() -> new EntityNotFoundException("Lesson not found"));
        requireEnrollment(lesson.getModule().getCourse().getId());
        var data = assessments.findByLessonId(lessonId).stream().map(this::assessmentView).toList();
        return ApiResponse.success("Lesson assessments retrieved", data);
    }

    @GetMapping("/{assessmentId}")
    @Transactional(readOnly = true)
    public ApiResponse<Map<String,Object>> get(@PathVariable UUID assessmentId) {
        var assessment = assessments.findById(assessmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assessment not found"));
        requireEnrollment(assessment.getCourse().getId());
        return ApiResponse.success("Assessment retrieved", assessmentView(assessment));
    }

    @PostMapping("/{assessmentId}/submit")
    @Transactional
    public ApiResponse<AssessmentResultResponse> submit(@PathVariable UUID assessmentId,
                                                          @Valid @RequestBody SubmitAssessmentRequest request) {
        var assessment=assessments.findById(assessmentId).orElseThrow(()->new EntityNotFoundException("Assessment not found"));
        UUID currentUserId=SecurityUtils.getCurrentUserId();
        requireEnrollment(assessment.getCourse().getId());
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

    private void requireEnrollment(UUID courseId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (!enrollments.existsByCourseIdAndUserId(courseId, currentUserId)) {
            throw new AccessDeniedException("You are not enrolled in this course");
        }
    }

    private Map<String,Object> assessmentView(com.masterlearning.platform.modules.assessment.entity.Assessment assessment) {
        List<Map<String,Object>> questionViews = questions.findByAssessmentId(assessment.getId()).stream().map(question -> {
            List<Map<String,Object>> optionViews = options.findByQuestionId(question.getId()).stream().map(option -> {
                Map<String,Object> view = new LinkedHashMap<>();
                view.put("id", option.getId());
                view.put("optionText", option.getOptionText());
                return view;
            }).toList();
            Map<String,Object> view = new LinkedHashMap<>();
            view.put("id", question.getId());
            view.put("questionText", question.getQuestionText());
            view.put("questionType", question.getQuestionType());
            view.put("points", question.getPoints());
            view.put("options", optionViews);
            return view;
        }).toList();

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        long attemptCount = attempts.countByAssessmentIdAndUserId(assessment.getId(), currentUserId);
        boolean passed = attempts.findTopByAssessmentIdAndUserIdAndPassedTrueOrderBySubmittedAtDesc(
                assessment.getId(), currentUserId).isPresent();

        Map<String,Object> view = new LinkedHashMap<>();
        view.put("id", assessment.getId());
        view.put("title", assessment.getTitle());
        view.put("level", assessment.getAssessmentLevel());
        view.put("passingScore", assessment.getPassingScore());
        view.put("maxAttempts", assessment.getMaxAttempts());
        view.put("attemptsUsed", attemptCount);
        view.put("passed", passed);
        view.put("questions", questionViews);
        return view;
    }

    private String masteryLevel(int score){
        if(score>=90) return "MASTERED";
        if(score>=70) return "PROFICIENT";
        if(score>=50) return "DEVELOPING";
        return "NEEDS_REVIEW";
    }
}
