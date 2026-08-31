package com.masterlearning.platform.modules.assessment.controller;
import com.masterlearning.platform.common.api.ApiResponse; import com.masterlearning.platform.modules.assessment.dto.response.StudentQuestionResponse; import com.masterlearning.platform.modules.assessment.repository.*; import jakarta.persistence.EntityNotFoundException; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/v1/student/assessments") public class StudentAssessmentQuestionsController{
 private final AssessmentRepository assessments; private final QuestionRepository questions; private final QuestionOptionRepository options;
 public StudentAssessmentQuestionsController(AssessmentRepository a,QuestionRepository q,QuestionOptionRepository o){assessments=a;questions=q;options=o;}
 @GetMapping("/{assessmentId}/questions") public ApiResponse<List<StudentQuestionResponse>> questions(@PathVariable UUID assessmentId){
  assessments.findById(assessmentId).orElseThrow(()->new EntityNotFoundException("Assessment not found"));
  var data=questions.findByAssessmentId(assessmentId).stream().map(q->new StudentQuestionResponse(q.getId(),q.getQuestionText(),q.getQuestionType().name(),q.getPoints(),options.findByQuestionId(q.getId()).stream().map(o->new StudentQuestionResponse.Option(o.getId(),o.getOptionText())).toList())).toList();
  return ApiResponse.success("Assessment questions retrieved",data);
 }
}