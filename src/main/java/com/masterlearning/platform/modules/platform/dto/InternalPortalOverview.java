package com.masterlearning.platform.modules.platform.dto;

import java.time.Instant;
import java.util.Map;

public record InternalPortalOverview(
        String status,
        String service,
        Instant timestamp,
        long totalUsers,
        long activeUsers,
        long totalOrganizations,
        long activeOrganizations,
        Map<String, Long> usersByRole,
        long newUsersLast30Days,
        long newOrganizationsLast30Days
) {}
