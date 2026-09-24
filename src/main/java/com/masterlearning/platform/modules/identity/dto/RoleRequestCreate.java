package com.masterlearning.platform.modules.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleRequestCreate(
        @NotBlank @Size(max=50) String requestedRole,
        @Size(max=1000) String reason
) {}
