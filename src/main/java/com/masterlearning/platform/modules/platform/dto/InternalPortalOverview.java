package com.masterlearning.platform.modules.platform.dto;

import java.time.Instant;

public record InternalPortalOverview(
        String status,
        String service,
        Instant timestamp,
        long totalUsers,
        long totalOrganizations
) {}
