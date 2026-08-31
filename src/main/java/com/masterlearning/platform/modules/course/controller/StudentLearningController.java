package com.masterlearning.platform.modules.course.controller;
import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.course.dto.response.*;
import com.masterlearning.platform.modules.course.entity.*;
import com.masterlearning.platform.modules.course.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/v1/student/learning")
public class StudentLearningController {
 private final EnrollmentRepository enrollments; private final CourseModuleRepository modules; private final LessonRepository lessons; private final LessonProgressRepository progress;
 public StudentLearningController(EnrollmentRepository e,CourseModuleRepository m,LessonRepository l,LessonProgressRepository p){enrollments=e;modules=m;lessons=l;progress=p;}

 @GetMapping("/enrollments/{enrollmentId}")
 public ApiResponse<CourseLearningResponse> course(@PathVariable UUID enrollmentId){
   var e=enrollments.findById(enrollmentId).orElseThrow(()->new EntityNotFoundException("Enrollment not found"));
   var list=modules.findByCourseIdOrderBySortOrderAsc(e.getCourse().getId()).stream().map(m->new ModuleResponse(m.getId(),m.getTitle(),m.getSortOrder(),lessons.findByModuleIdOrderBySortOrderAsc(m.getId()).stream().map(l->{
     boolean done=progress.findByEnrollmentIdAndLessonId(enrollmentId,l.getId()).map(LessonProgress::isCompleted).orElse(false);
     return new LessonResponse(l.getId(),l.getTitle(),l.getContentType(),l.getContent(),l.getSortOrder(),done);
   }).toList())).toList();
   return ApiResponse.success("Course learning content retrieved",new CourseLearningResponse(e.getCourse().getId(),e.getCourse().getTitle(),e.getCourse().getDescription(),e.getProgressPercent(),list));
 }

 @PostMapping("/enrollments/{enrollmentId}/lessons/{lessonId}/complete")
 public ApiResponse<EnrollmentResponse> complete(@PathVariable UUID enrollmentId,@PathVariable UUID lessonId){
   var e=enrollments.findById(enrollmentId).orElseThrow(()->new EntityNotFoundException("Enrollment not found"));
   var l=lessons.findById(lessonId).orElseThrow(()->new EntityNotFoundException("Lesson not found"));
   var lp=progress.findByEnrollmentIdAndLessonId(enrollmentId,lessonId).orElseGet(()->progress.save(new LessonProgress(e,l)));
   lp.complete();
   long completed=progress.countByEnrollmentIdAndCompletedTrue(enrollmentId);
   long total=modules.findByCourseIdOrderBySortOrderAsc(e.getCourse().getId()).stream().mapToLong(m->lessons.findByModuleIdOrderBySortOrderAsc(m.getId()).size()).sum();
   int percent=total==0?0:(int)Math.round(completed*100.0/total);
   e.updateProgress(percent);
   return ApiResponse.success("Lesson completed and progress updated",new EnrollmentResponse(e.getId(),e.getProgressPercent()));
 }
}