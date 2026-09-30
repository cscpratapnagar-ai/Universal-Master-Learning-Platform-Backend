package com.masterlearning.platform.modules.privateTeacher;
import com.masterlearning.platform.common.api.ApiResponse; import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController @RequestMapping("/api/v1/private-teacher/matching")
@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_ORG_ADMIN','ROLE_TEACHER','ROLE_INSTRUCTOR','ROLE_LEARNER','ROLE_STUDENT')")
public class PrivateTeacherMatchController {
 private final PrivateTeacherMatchService service; public PrivateTeacherMatchController(PrivateTeacherMatchService s){service=s;}
 @PostMapping("/search") public ApiResponse<List<PrivateTeacherMatchResponse>> search(@Valid @RequestBody PrivateTeacherMatchRequest request){
   return ApiResponse.success("Private teacher matches loaded",service.match(request));
 }
}