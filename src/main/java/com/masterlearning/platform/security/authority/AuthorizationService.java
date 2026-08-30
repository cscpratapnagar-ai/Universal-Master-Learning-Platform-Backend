package com.masterlearning.platform.security.authority;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("authorization")
public class AuthorizationService {

    public UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof CurrentUserPrincipal principal)) {
            throw new IllegalStateException("No authenticated user is available");
        }

        return principal.userId();
    }

    public boolean isCurrentUser(UUID userId) {
        return userId != null && userId.equals(currentUserId());
    }

    public boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(granted -> authority.equals(granted.getAuthority()));
    }

    public boolean hasAnyAuthority(String... authorities) {
        if (authorities == null || authorities.length == 0) {
            return false;
        }

        for (String authority : authorities) {
            if (hasAuthority(authority)) {
                return true;
            }
        }

        return false;
    }

    public boolean canAccessUser(UUID userId) {
        return isCurrentUser(userId)
                || hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_PLATFORM_ADMIN", "USER_READ_ALL");
    }
}
