package com.masterlearning.platform.modules.platform.dto;

import java.time.Instant;
import java.util.Map;

public record AdminPortalOverview(
        String status,
        Instant timestamp,
        long totalUsers,
        long activeUsers,
        long newUsersLast30Days,
        long totalOrganizations,
        long activeOrganizations,
        long newOrganizationsLast30Days,
        Map<String, Long> usersByRole
) {}
