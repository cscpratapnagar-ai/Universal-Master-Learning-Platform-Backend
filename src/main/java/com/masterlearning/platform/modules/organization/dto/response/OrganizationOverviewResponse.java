package com.masterlearning.platform.modules.organization.dto.response;

import com.masterlearning.platform.modules.organization.entity.OrganizationStatus;

import java.util.UUID;

public record OrganizationOverviewResponse(
        UUID organizationId,
        String organizationName,
        String organizationCode,
        OrganizationStatus status,
        boolean active,
        long totalMembers,
        long activeMembers,
        long inactiveMembers,
        long totalCourses,
        long publishedCourses,
        long draftCourses,
        long archivedCourses
) {}
