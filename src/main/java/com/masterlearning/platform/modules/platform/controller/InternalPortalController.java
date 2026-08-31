package com.masterlearning.platform.modules.platform.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.organization.repository.OrganizationRepository;
import com.masterlearning.platform.modules.platform.dto.InternalPortalOverview;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/internal")
public class InternalPortalController {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public InternalPortalController(
            UserRepository userRepository,
            OrganizationRepository organizationRepository
    ) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    @GetMapping("/overview")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<InternalPortalOverview> overview() {
        InternalPortalOverview overview = new InternalPortalOverview(
                "ONLINE",
                "universal-master-learning-platform-backend",
                Instant.now(),
                userRepository.count(),
                userRepository.countByEnabledTrue(),
                organizationRepository.count(),
                organizationRepository.countByActiveTrue(),
                usersByRole(),
                userRepository.countByCreatedAtAfter(Instant.now().minus(30, ChronoUnit.DAYS)),
                organizationRepository.countByCreatedAtAfter(Instant.now().minus(30, ChronoUnit.DAYS))
        );

        return ApiResponse.success("Internal portal overview loaded", overview);
    }

    private Map<String, Long> usersByRole() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : userRepository.countUsersByRole()) {
            result.put((String) row[0], ((Number) row[1]).longValue());
        }
        return result;
    }
}
