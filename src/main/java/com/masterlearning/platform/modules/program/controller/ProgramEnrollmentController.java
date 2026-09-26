package com.masterlearning.platform.modules.program.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.organization.repository.OrganizationMemberRepository;
import com.masterlearning.platform.modules.organization.security.OrganizationAuthorizationService;
import com.masterlearning.platform.modules.program.entity.ProgramEnrollment;
import com.masterlearning.platform.modules.program.entity.ProgramStatus;
import com.masterlearning.platform.modules.program.entity.ProgramMilestoneStatus;
import com.masterlearning.platform.modules.program.repository.ProgramEnrollmentRepository;
import com.masterlearning.platform.modules.program.repository.ProgramRepository;
import com.masterlearning.platform.modules.program.repository.ProgramMilestoneRepository;
import com.masterlearning.platform.modules.program.repository.LearningPathCourseRepository;
import com.masterlearning.platform.modules.program.repository.ProgramActivityRepository;
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
 private final OrganizationMemberRepository members; private final OrganizationAuthorizationService authorization; private final LearningPathCourseRepository pathCourses; private final EnrollmentRepository courseEnrollments; private final CourseRepository courses; private final ProgramMilestoneRepository milestoneRepository; private final ProgramActivityRepository activities;
 public ProgramEnrollmentController(ProgramEnrollmentRepository e,ProgramRepository p,UserRepository u,OrganizationMemberRepository m,OrganizationAuthorizationService a,LearningPathCourseRepository pc,EnrollmentRepository ce,CourseRepository cr,ProgramMilestoneRepository mr,ProgramActivityRepository pa){enrollments=e;programs=p;users=u;members=m;authorization=a;pathCourses=pc;courseEnrollments=ce;courses=cr;milestoneRepository=mr;activities=pa;}

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
   activities.save(new com.masterlearning.platform.modules.program.entity.ProgramActivity(p,"LEARNER_ENROLLED","Learner "+userId+" enrolled in the project",String.valueOf(userId)));
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

 @GetMapping("/mine/{programId}/execution") @PreAuthorize("isAuthenticated()") @Transactional
 public ApiResponse<Map<String,Object>> mineExecution(@PathVariable UUID programId){
   UUID uid=com.masterlearning.platform.security.util.SecurityUtils.getCurrentUserId();
   var e=enrollments.findByProgramIdAndUserId(programId,uid).orElseThrow(()->new AccessDeniedException("You are not enrolled in this program"));
   syncProgress(e,uid);
   var milestoneRepo=milestoneRepository;
   var today=java.time.LocalDate.now();
   var ms=milestoneRepo.findByProgramIdOrderBySortOrderAscDueDateAsc(programId).stream().map(m->{Map<String,Object> x=new LinkedHashMap<>();x.put("id",m.getId());x.put("title",m.getTitle());x.put("description",m.getDescription());x.put("dueDate",m.getDueDate());x.put("sortOrder",m.getSortOrder());x.put("status",m.getStatus().name());x.put("overdue",m.getDueDate()!=null&&m.getDueDate().isBefore(today)&&m.getStatus()!=ProgramMilestoneStatus.COMPLETED&&m.getStatus()!=ProgramMilestoneStatus.CANCELLED);return x;}).toList();
   long completedMilestones=ms.stream().filter(x->"COMPLETED".equals(x.get("status"))).count(); long overdueMilestones=ms.stream().filter(x->Boolean.TRUE.equals(x.get("overdue"))).count(); Map<String,Object> data=data(e); data.put("milestones",ms); data.put("milestoneCount",ms.size()); data.put("completedMilestoneCount",completedMilestones); data.put("overdueMilestoneCount",overdueMilestones); data.put("nextMilestone",ms.stream().filter(x->!"COMPLETED".equals(x.get("status"))&&!"CANCELLED".equals(x.get("status"))).findFirst().orElse(null)); return ApiResponse.success("Project execution retrieved",data);
 }

 @GetMapping("/mine/{programId}/activity") @PreAuthorize("isAuthenticated()") @Transactional(readOnly=true)
 public ApiResponse<List<Map<String,Object>>> mineActivity(@PathVariable UUID programId){
   UUID uid=com.masterlearning.platform.security.util.SecurityUtils.getCurrentUserId();
   var e=enrollments.findByProgramIdAndUserId(programId,uid).orElseThrow(()->new AccessDeniedException("You are not enrolled in this program"));
   return ApiResponse.success("Project learner activity retrieved",activities.findTop100ByProgramIdOrderByCreatedAtDesc(programId).stream().map(a->{Map<String,Object> x=new LinkedHashMap<>();x.put("id",a.getId());x.put("action",a.getAction());x.put("details",a.getDetails());x.put("actor",a.getActor());x.put("createdAt",a.getCreatedAt());return x;}).toList());
 }

 @GetMapping("/mine/{programId}/workspace") @PreAuthorize("isAuthenticated()") @Transactional(readOnly=true)
 public ApiResponse<Map<String,Object>> mineWorkspace(@PathVariable UUID programId){
   UUID uid=com.masterlearning.platform.security.util.SecurityUtils.getCurrentUserId();
   var e=enrollments.findByProgramIdAndUserId(programId,uid).orElseThrow(()->new AccessDeniedException("You are not enrolled in this program"));
   var courses=pathCourses.findByLearningPathProgramIdOrderByLearningPathTitleAscSortOrderAsc(programId).stream()
      .map(x->{Map<String,Object> m=new LinkedHashMap<>();m.put("courseId",x.getCourse().getId());m.put("title",x.getCourse().getTitle());m.put("status",x.getCourse().getStatus());m.put("sortOrder",x.getSortOrder());m.put("enrolled",courseEnrollments.findByCourseIdAndUserId(x.getCourse().getId(),uid).isPresent());m.put("progressPercent",courseEnrollments.findByCourseIdAndUserId(x.getCourse().getId(),uid).map(z->z.getProgressPercent()).orElse(0));m.put("enrollmentId",courseEnrollments.findByCourseIdAndUserId(x.getCourse().getId(),uid).map(z->z.getId()).orElse(null));return m;}).toList();
   Map<String,Object> data=data(e); data.put("courses",courses); return ApiResponse.success("Project workspace retrieved",data);
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
 private Map<String,Object> data(ProgramEnrollment e){Map<String,Object> x=new LinkedHashMap<>();x.put("id",e.getId());x.put("programId",e.getProgram().getId());x.put("programTitle",e.getProgram().getTitle());x.put("organizationId",e.getProgram().getOrganization()==null?null:e.getProgram().getOrganization().getId());x.put("userId",e.getUser().getId());x.put("userEmail",e.getUser().getEmail());x.put("userFirstName",e.getUser().getFirstName());x.put("userLastName",e.getUser().getLastName());x.put("status",e.getStatus());x.put("progressPercent",e.getProgressPercent());x.put("completedAt",e.getCompletedAt());return x;}
}