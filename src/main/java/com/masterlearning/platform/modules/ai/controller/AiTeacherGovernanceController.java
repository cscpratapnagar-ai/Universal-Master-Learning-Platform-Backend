package com.masterlearning.platform.modules.ai.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.ai.dto.response.AiTeacherGovernanceOverviewResponse;
import com.masterlearning.platform.modules.ai.service.AiTeacherGovernanceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/ai-teacher")
public class AiTeacherGovernanceController {

    private final AiTeacherGovernanceService governance;

    public AiTeacherGovernanceController(AiTeacherGovernanceService governance) {
        this.governance = governance;
    }

    @GetMapping("/overview")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_ORG_ADMIN')")
    public ApiResponse<AiTeacherGovernanceOverviewResponse> overview() {
        return ApiResponse.success(
                "AI Teacher governance overview loaded",
                governance.getOverview());
    }
}
