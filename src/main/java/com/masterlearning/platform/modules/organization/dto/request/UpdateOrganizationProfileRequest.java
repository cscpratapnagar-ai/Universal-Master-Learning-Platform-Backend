package com.masterlearning.platform.modules.organization.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record UpdateOrganizationProfileRequest(
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must contain lowercase letters, numbers and hyphens only")
        @Size(min = 3, max = 120) String slug,
        @Size(max = 250) String legalName,
        @Size(max = 200) String displayName,
        @Size(max = 80) String organizationType,
        @Size(max = 120) String registrationNumber,
        @PastOrPresent LocalDate establishedDate,
        @Email @Size(max = 254) String primaryEmail,
        @Size(max = 40) String primaryPhone,
        @Size(max = 40) String alternatePhone,
        @Size(max = 500) String website,
        @Size(max = 500) String addressLine,
        @Size(max = 100) String country,
        @Size(max = 100) String state,
        @Size(max = 100) String city,
        @Size(max = 100) String district,
        @Size(max = 30) String postalCode,
        @Size(max = 1000) String logoUrl,
        @Size(max = 1000) String coverImageUrl,
        @Pattern(regexp = "^#?[0-9A-Fa-f]{6}$", message = "Primary color must be a 6-digit hex color") String primaryColor,
        @Pattern(regexp = "^#?[0-9A-Fa-f]{6}$", message = "Secondary color must be a 6-digit hex color") String secondaryColor
) {}
