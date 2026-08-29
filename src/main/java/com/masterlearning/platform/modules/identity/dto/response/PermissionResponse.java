package com.masterlearning.platform.modules.identity.dto.response;

import java.util.UUID;

public record PermissionResponse(
        UUID id,
        String code,
        String name,
        String description
) {
}