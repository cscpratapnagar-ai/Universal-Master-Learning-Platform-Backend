package com.masterlearning.platform.modules.program.controller;
import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.course.repository.CourseRepository;
import com.masterlearning.platform.modules.program.dto.request.*;
import com.masterlearning.platform.modules.program.entity.*;
import com.masterlearning.platform.modules.program.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/programs")
public class ProgramArchitectureController {
 private final LearningPathRepository paths; private final LearningPathCourseRepository pathCourses; private final CourseRepository courses;
 private final com.masterlearning.platform.modules.organization.security.OrganizationAuthorizationService organizationAuthorization;
 public ProgramArchitectureController(LearningPathRepository p,LearningPathCourseRepository pc,CourseRepository c,com.masterlearning.platform.modules.organization.security.OrganizationAuthorizationService oa){paths=p;pathCourses=pc;courses=c;organizationAuthorization=oa;}

 @PutMapping("/paths/{pathId}") @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR','ORG_ADMIN')")
 public ApiResponse<Map<String,Object>> updatePath(@PathVariable UUID pathId,@Valid @RequestBody UpdateLearningPathRequest r){var p=findPath(pathId);p.update(r.title().trim(),r.description());return ApiResponse.success("Learning path updated",pathData(p));}

 @GetMapping("/paths/{pathId}/courses") @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR','ORG_ADMIN')")
 public ApiResponse<List<Map<String,Object>>> getPathCourses(@PathVariable UUID pathId){var p=findPath(pathId);return ApiResponse.success("Learning path courses retrieved",pathCourses.findByLearningPathIdOrderBySortOrderAsc(pathId).stream().map(this::courseData).toList());}

 @PutMapping("/paths/{pathId}/courses/{courseId}/order") @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR','ORG_ADMIN')")
 public ApiResponse<Map<String,Object>> reorder(@PathVariable UUID pathId,@PathVariable UUID courseId,@Valid @RequestBody UpdatePathCourseOrderRequest r){findPath(pathId);var link=pathCourses.findByLearningPathIdAndCourseId(pathId,courseId).orElseThrow(()->new EntityNotFoundException("Course is not assigned to this learning path"));link.updateSortOrder(r.sortOrder());return ApiResponse.success("Course order updated",courseData(link));}

 @DeleteMapping("/paths/{pathId}/courses/{courseId}") @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR','ORG_ADMIN')")
 public ApiResponse<Void> removeCourse(@PathVariable UUID pathId,@PathVariable UUID courseId){findPath(pathId);var link=pathCourses.findByLearningPathIdAndCourseId(pathId,courseId).orElseThrow(()->new EntityNotFoundException("Course is not assigned to this learning path"));pathCourses.delete(link);return ApiResponse.success("Course removed from learning path",null);}

 private LearningPath findPath(UUID id){var p=paths.findWithProgramAndOrganizationById(id).orElseThrow(()->new EntityNotFoundException("Learning path not found"));var org=p.getProgram().getOrganization();if(org==null || !organizationAuthorization.canAccessOrganization(org.getId()) && !isSuperAdmin())throw new AccessDeniedException("You do not have access to this learning path");return p;}
 private boolean isSuperAdmin(){var a=org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();return a!=null&&a.getAuthorities().stream().anyMatch(x->"ROLE_SUPER_ADMIN".equals(x.getAuthority()));}
 private Map<String,Object> pathData(LearningPath p){var m=new LinkedHashMap<String,Object>();m.put("id",p.getId());m.put("title",p.getTitle());m.put("description",p.getDescription()==null?"":p.getDescription());m.put("programId",p.getProgram().getId());return m;}
 private Map<String,Object> courseData(LearningPathCourse l){var m=new LinkedHashMap<String,Object>();m.put("id",l.getId());m.put("courseId",l.getCourse().getId());m.put("title",l.getCourse().getTitle());m.put("slug",l.getCourse().getSlug());m.put("status",l.getCourse().getStatus().name());m.put("sortOrder",l.getSortOrder());return m;}
}