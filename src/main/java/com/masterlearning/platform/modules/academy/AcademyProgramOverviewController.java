package com.masterlearning.platform.modules.academy;

import com.masterlearning.platform.common.api.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/academy/programs")
@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_ORG_ADMIN')")
public class AcademyProgramOverviewController {

    private final AcademyProgramOverviewService service;

    public AcademyProgramOverviewController(AcademyProgramOverviewService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public ApiResponse<AcademyProgramOverviewResponse> overview() {
        return ApiResponse.success("Academy program overview loaded", service.getOverview());
    }
}
