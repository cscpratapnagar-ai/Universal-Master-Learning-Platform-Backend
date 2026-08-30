package com.masterlearning.platform.modules.organization.dto.request;

import com.masterlearning.platform.modules.organization.entity.OrganizationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrganizationStatusRequest(@NotNull OrganizationStatus status) {}
