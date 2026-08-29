package com.masterlearning.platform.modules.organization.dto.request;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record AddOrganizationMemberRequest(@NotNull UUID userId){}