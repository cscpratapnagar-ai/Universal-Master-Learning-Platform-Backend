package com.masterlearning.platform.modules.platform.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.organization.repository.OrganizationRepository;
import com.masterlearning.platform.modules.platform.dto.AdminPortalOverview;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminPortalController {
    private final UserRepository users;
    private final OrganizationRepository organizations;

    public AdminPortalController(UserRepository users, OrganizationRepository organizations) {
        this.users = users;
        this.organizations = organizations;
    }

    @GetMapping("/overview")
    public ApiResponse<AdminPortalOverview> overview() {
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
        var roles = new LinkedHashMap<String, Long>();
        users.countUsersByRole().forEach(row -> roles.put(String.valueOf(row[0]), ((Number) row[1]).longValue()));

        return ApiResponse.success("Admin portal overview loaded", new AdminPortalOverview(
                "ONLINE", Instant.now(), users.count(), users.countByEnabledTrue(),
                users.countByCreatedAtAfter(since), organizations.count(),
                organizations.countByActiveTrue(), organizations.countByCreatedAtAfter(since), roles
        ));
    }
}
