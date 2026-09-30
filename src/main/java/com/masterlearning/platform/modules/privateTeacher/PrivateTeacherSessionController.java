package com.masterlearning.platform.modules.privateTeacher;
import com.masterlearning.platform.common.api.ApiResponse; import com.masterlearning.platform.modules.user.repository.UserRepository; import jakarta.validation.Valid; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.security.core.annotation.AuthenticationPrincipal; import org.springframework.security.core.userdetails.UserDetails; import org.springframework.web.bind.annotation.*; import java.util.UUID;
@RestController @RequestMapping("/api/v1/private-teacher/sessions") @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_ORG_ADMIN','ROLE_TEACHER','ROLE_INSTRUCTOR','ROLE_LEARNER','ROLE_STUDENT')")
public class PrivateTeacherSessionController {
 private final PrivateTeacherSessionService service; private final UserRepository users;
 public PrivateTeacherSessionController(PrivateTeacherSessionService s,UserRepository u){service=s;users=u;}
 @PostMapping("/request") public ApiResponse<PrivateTeacherSessionResponse> request(@AuthenticationPrincipal UserDetails p,@Valid @RequestBody PrivateTeacherSessionRequest r){
  UUID learner=users.findByEmailIgnoreCase(p.getUsername()).orElseThrow().getId(); return ApiResponse.success("Private teacher session requested",service.request(learner,r));
 }
 @PostMapping("/{id}/confirm") public ApiResponse<PrivateTeacherSessionResponse> confirm(@PathVariable UUID id){return ApiResponse.success("Private teacher session confirmed",service.confirm(id));}
 @PostMapping("/{id}/cancel") public ApiResponse<PrivateTeacherSessionResponse> cancel(@PathVariable UUID id){return ApiResponse.success("Private teacher session cancelled",service.cancel(id));}
 @PostMapping("/{id}/complete") public ApiResponse<PrivateTeacherSessionResponse> complete(@PathVariable UUID id){return ApiResponse.success("Private teacher session completed",service.complete(id));}
}