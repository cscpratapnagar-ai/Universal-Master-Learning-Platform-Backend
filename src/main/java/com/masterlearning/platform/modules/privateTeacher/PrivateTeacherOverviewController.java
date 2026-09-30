package com.masterlearning.platform.modules.privateTeacher;

import com.masterlearning.platform.common.api.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/private-teacher")
@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_ORG_ADMIN', 'ROLE_TEACHER', 'ROLE_INSTRUCTOR')")
public class PrivateTeacherOverviewController {

    private final PrivateTeacherOverviewService service;

    public PrivateTeacherOverviewController(PrivateTeacherOverviewService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public ApiResponse<PrivateTeacherOverviewResponse> overview() {
        return ApiResponse.success("Private teacher overview loaded", service.getOverview());
    }
}
