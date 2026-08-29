package com.masterlearning.platform.security.authority;

import java.util.UUID;

public record CurrentUserPrincipal(
        UUID userId,
        String email
) {
}