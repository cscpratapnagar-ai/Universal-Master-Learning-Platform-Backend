package com.masterlearning.platform.modules.identity.dto.response;

import java.util.Set;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        String code,
        String name,
        String description,
        boolean systemRole,
        Set<String> permissions
) {
}