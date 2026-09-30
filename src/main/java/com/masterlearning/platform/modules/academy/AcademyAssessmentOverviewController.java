package com.masterlearning.platform.modules.academy;

import com.masterlearning.platform.common.api.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/academy/assessments")
@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_ORG_ADMIN')")
public class AcademyAssessmentOverviewController {

    private final AcademyAssessmentOverviewService service;

    public AcademyAssessmentOverviewController(AcademyAssessmentOverviewService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public ApiResponse<AcademyAssessmentOverviewResponse> overview() {
        return ApiResponse.success("Academy assessment overview loaded", service.getOverview());
    }
}
