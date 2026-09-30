package com.masterlearning.platform.modules.privateTeacher;
import com.masterlearning.platform.common.api.ApiResponse; import com.masterlearning.platform.modules.user.repository.UserRepository; import jakarta.validation.Valid; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.security.core.annotation.AuthenticationPrincipal; import org.springframework.security.core.userdetails.UserDetails; import org.springframework.web.bind.annotation.*; import java.util.UUID;
@RestController @RequestMapping("/api/v1/private-teacher") @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_ORG_ADMIN','ROLE_TEACHER','ROLE_INSTRUCTOR')")
public class PrivateTeacherProfileController {
 private final PrivateTeacherProfileService service; private final UserRepository users;
 public PrivateTeacherProfileController(PrivateTeacherProfileService s,UserRepository u){service=s;users=u;}
 private UUID id(UserDetails p){return users.findByEmailIgnoreCase(p.getUsername()).orElseThrow().getId();}
 @GetMapping("/profile/me") public ApiResponse<PrivateTeacherProfileResponse> get(@AuthenticationPrincipal UserDetails p){return ApiResponse.success("Private teacher profile loaded",service.get(id(p)));}
 @PutMapping("/profile/me") public ApiResponse<PrivateTeacherProfileResponse> save(@AuthenticationPrincipal UserDetails p,@Valid @RequestBody PrivateTeacherProfileRequest r){return ApiResponse.success("Private teacher profile saved",service.upsert(id(p),r));}
}