package com.masterlearning.platform.security.util;

import com.masterlearning.platform.security.authority.CurrentUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UUID getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof CurrentUserPrincipal principal)) {
            throw new IllegalStateException("No authenticated user available");
        }

        return principal.userId();
    }

    public static String getCurrentUserEmail() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof CurrentUserPrincipal principal)) {
            throw new IllegalStateException("No authenticated user available");
        }

        return principal.email();
    }
}