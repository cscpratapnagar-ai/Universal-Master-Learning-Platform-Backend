package com.masterlearning.platform.modules.academy;

import com.masterlearning.platform.common.api.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/academy")
@PreAuthorize("isAuthenticated()")
public class AcademyOverviewController {
    private final AcademyOverviewService service;

    public AcademyOverviewController(AcademyOverviewService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public ApiResponse<AcademyOverviewResponse> overview() {
        return ApiResponse.success("Academy overview loaded", service.overview());
    }
}
