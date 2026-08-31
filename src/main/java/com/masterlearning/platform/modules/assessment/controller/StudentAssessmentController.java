package com.masterlearning.platform.modules.assessment.controller;
import com.masterlearning.platform.common.api.ApiResponse; import com.masterlearning.platform.modules.assessment.dto.request.SubmitAssessmentRequest; import com.masterlearning.platform.modules.assessment.dto.response.AssessmentResultResponse; import com.masterlearning.platform.modules.assessment.entity.*; import com.masterlearning.platform.modules.assessment.repository.*; import com.masterlearning.platform.modules.user.repository.UserRepository; import jakarta.persistence.EntityNotFoundException; import jakarta.validation.Valid; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/v1/student/assessments") public class StudentAssessmentController {
 private final AssessmentRepository assessments; private final QuestionRepository questions; private final QuestionOptionRepository options; private final AssessmentAttemptRepository attempts; private final UserRepository users;
 public StudentAssessmentController(AssessmentRepository a,QuestionRepository q,QuestionOptionRepository o,AssessmentAttemptRepository at,UserRepository u){assessments=a;questions=q;options=o;attempts=at;users=u;}
 @PostMapping("/{assessmentId}/submit") public ApiResponse<AssessmentResultResponse> submit(@PathVariable UUID assessmentId,@Valid @RequestBody SubmitAssessmentRequest r){
  var a=assessments.findById(assessmentId).orElseThrow(()->new EntityNotFoundException("Assessment not found")); var u=users.findById(r.userId()).orElseThrow(()->new EntityNotFoundException("User not found"));
  var qs=questions.findByAssessmentId(assessmentId); if(qs.isEmpty()) throw new IllegalArgumentException("Assessment has no questions");
  int total=qs.stream().mapToInt(Question::getPoints).sum(), earned=0, correct=0;
  for(var q:qs){UUID selected=r.answers().get(q.getId()); if(selected!=null && options.findByQuestionId(q.getId()).stream().anyMatch(o->o.getId().equals(selected)&&o.isCorrect())){earned+=q.getPoints();correct++;}}
  int score=(int)Math.round(earned*100.0/total); boolean passed=score>=a.getPassingScore(); var attempt=attempts.save(new AssessmentAttempt(a,u,score,passed));
  return ApiResponse.success("Assessment submitted",new AssessmentResultResponse(attempt.getId(),score,passed,correct,qs.size()));
 }
}