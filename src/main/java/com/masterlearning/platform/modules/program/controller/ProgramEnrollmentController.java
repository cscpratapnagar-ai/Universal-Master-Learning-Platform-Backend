package com.masterlearning.platform.modules.program.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.organization.repository.OrganizationMemberRepository;
import com.masterlearning.platform.modules.organization.security.OrganizationAuthorizationService;
import com.masterlearning.platform.modules.program.entity.ProgramEnrollment;
import com.masterlearning.platform.modules.program.entity.ProgramStatus;
import com.masterlearning.platform.modules.program.repository.ProgramEnrollmentRepository;
import com.masterlearning.platform.modules.program.repository.ProgramRepository;
import com.masterlearning.platform.modules.program.repository.LearningPathCourseRepository;
import com.masterlearning.platform.modules.course.repository.EnrollmentRepository;
import com.masterlearning.platform.modules.course.repository.CourseRepository;
import com.masterlearning.platform.modules.course.entity.CourseStatus;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/program-enrollments")
public class ProgramEnrollmentController {
 private final ProgramEnrollmentRepository enrollments; private final ProgramRepository programs; private final UserRepository users;
 private final OrganizationMemberRepository members; private final OrganizationAuthorizationService authorization; private final LearningPathCourseRepository pathCourses; private final EnrollmentRepository courseEnrollments; private final CourseRepository courses;
 public ProgramEnrollmentController(ProgramEnrollmentRepository e,ProgramRepository p,UserRepository u,OrganizationMemberRepository m,OrganizationAuthorizationService a,LearningPathCourseRepository pc,EnrollmentRepository ce,CourseRepository cr){enrollments=e;programs=p;users=u;members=m;authorization=a;pathCourses=pc;courseEnrollments=ce;courses=cr;}

 @PostMapping("/{programId}/users/{userId}") @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','ORG_ADMIN')")
 @Transactional public ApiResponse<Map<String,Object>> enroll(@PathVariable UUID programId,@PathVariable UUID userId){
   var p=programs.findWithOrganizationById(programId).orElseThrow(()->new EntityNotFoundException("Program not found"));
   assertManageable(p);
   if(p.getStatus()!=ProgramStatus.PUBLISHED && p.getStatus()!=ProgramStatus.ACTIVE) throw new IllegalStateException("Only published or active programs can enroll learners");
   if(p.getOrganization()==null || !members.findByOrganizationIdAndUserId(p.getOrganization().getId(),userId).map(x->x.isActive()).orElse(false))
      throw new AccessDeniedException("Learner must be an active member of the program organization");
   var existing=enrollments.findByProgramIdAndUserId(programId,userId);
   var e=existing.orElseGet(()->enrollments.save(new ProgramEnrollment(p,users.findById(userId).orElseThrow(()->new EntityNotFoundException("User not found")))));
   autoEnrollPublishedCourses(p.getId(),userId);
   return ApiResponse.success("Learner enrolled in program",data(e));
 }

 @GetMapping("/{programId}") @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','ORG_ADMIN')")
 @Transactional(readOnly=true) public ApiResponse<List<Map<String,Object>>> list(@PathVariable UUID programId){
   var p=programs.findWithOrganizationById(programId).orElseThrow(()->new EntityNotFoundException("Program not found")); assertManageable(p);
   return ApiResponse.success("Program learners retrieved",enrollments.findByProgramIdOrderByCreatedAtAsc(programId).stream().map(this::data).toList());
 }

 @GetMapping("/mine") @PreAuthorize("isAuthenticated()") @Transactional
 public ApiResponse<List<Map<String,Object>>> mine(){
   UUID uid=com.masterlearning.platform.security.util.SecurityUtils.getCurrentUserId();
   var list=enrollments.findByUserIdOrderByCreatedAtDesc(uid); list.forEach(e->syncProgress(e,uid));
   return ApiResponse.success("My program enrollments retrieved",list.stream().map(this::data).toList());
 }

 @GetMapping("/mine/{programId}") @PreAuthorize("isAuthenticated()") @Transactional
 public ApiResponse<Map<String,Object>> mineProgram(@PathVariable UUID programId){
   UUID uid=com.masterlearning.platform.security.util.SecurityUtils.getCurrentUserId();
   var e=enrollments.findByProgramIdAndUserId(programId,uid).orElseThrow(()->new AccessDeniedException("You are not enrolled in this program"));
   syncProgress(e,uid); return ApiResponse.success("Program learning progress retrieved",data(e));
 }

 @PutMapping("/{enrollmentId}/cancel") @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','ORG_ADMIN')")
 @Transactional public ApiResponse<Map<String,Object>> cancel(@PathVariable UUID enrollmentId){
   var e=enrollments.findById(enrollmentId).orElseThrow(()->new EntityNotFoundException("Program enrollment not found")); assertManageable(e.getProgram()); e.cancel();
   return ApiResponse.success("Program enrollment cancelled",data(e));
 }

 private void autoEnrollPublishedCourses(UUID programId, UUID userId){
   var user=users.findById(userId).orElseThrow(()->new EntityNotFoundException("User not found"));
   pathCourses.findByLearningPathProgramIdOrderByLearningPathTitleAscSortOrderAsc(programId).stream()
      .map(x->x.getCourse()).filter(c->c.getStatus()==CourseStatus.PUBLISHED).distinct()
      .forEach(c-> { if(!courseEnrollments.existsByCourseIdAndUserId(c.getId(),userId)) courseEnrollments.save(new com.masterlearning.platform.modules.course.entity.Enrollment(c,user)); });
 }
 private void syncProgress(ProgramEnrollment e, UUID userId){
   var courseIds=pathCourses.findByLearningPathProgramIdOrderByLearningPathTitleAscSortOrderAsc(e.getProgram().getId()).stream().map(x->x.getCourse().getId()).distinct().toList();
   if(courseIds.isEmpty()){e.updateProgress(0);return;}
   long completed=courseIds.stream().filter(id->courseEnrollments.findByCourseIdAndUserId(id,userId).map(x->x.getProgressPercent()>=100).orElse(false)).count();
   e.updateProgress((int)Math.round(completed*100.0/courseIds.size())); enrollments.save(e);
 }
 private void assertManageable(com.masterlearning.platform.modules.program.entity.Program p){
   UUID oid=p.getOrganization()==null?null:p.getOrganization().getId();
   boolean superAdmin=org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()!=null &&
      org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(a->"ROLE_SUPER_ADMIN".equals(a.getAuthority()));
   if(!superAdmin && (oid==null || !authorization.canAccessOrganization(oid))) throw new AccessDeniedException("You do not have access to this organization");
 }
 private Map<String,Object> data(ProgramEnrollment e){Map<String,Object> x=new LinkedHashMap<>();x.put("id",e.getId());x.put("programId",e.getProgram().getId());x.put("programTitle",e.getProgram().getTitle());x.put("organizationId",e.getProgram().getOrganization()==null?null:e.getProgram().getOrganization().getId());x.put("userId",e.getUser().getId());x.put("status",e.getStatus());x.put("progressPercent",e.getProgressPercent());x.put("completedAt",e.getCompletedAt());return x;}
}