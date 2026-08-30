package com.masterlearning.platform.modules.organization.dto.response;

import com.masterlearning.platform.modules.organization.entity.OrganizationStatus;
import java.time.LocalDate;
import java.util.UUID;

public record OrganizationProfileResponse(
        UUID id, String code, String name, String description, boolean active,
        String slug, String legalName, String displayName, String organizationType,
        String registrationNumber, LocalDate establishedDate, String primaryEmail,
        String primaryPhone, String alternatePhone, String website, String addressLine,
        String country, String state, String city, String district, String postalCode,
        String logoUrl, String coverImageUrl, String primaryColor, String secondaryColor,
        OrganizationStatus status
) {}
