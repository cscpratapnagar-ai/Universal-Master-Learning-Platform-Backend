package com.masterlearning.platform.security.util;

import com.masterlearning.platform.security.authority.CurrentUserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityUtilsTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsCurrentUserIdAndEmailFromAuthenticatedPrincipal() {
        UUID userId = UUID.randomUUID();
        String email = "learner@example.com";
        var principal = new CurrentUserPrincipal(userId, email);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null));

        assertEquals(userId, SecurityUtils.getCurrentUserId());
        assertEquals(email, SecurityUtils.getCurrentUserEmail());
    }

    @Test
    void rejectsMissingAuthentication() {
        SecurityContextHolder.clearContext();

        assertThrows(IllegalStateException.class, SecurityUtils::getCurrentUserId);
        assertThrows(IllegalStateException.class, SecurityUtils::getCurrentUserEmail);
    }

    @Test
    void rejectsAuthenticationWithUnexpectedPrincipalType() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("learner@example.com", null));

        assertThrows(IllegalStateException.class, SecurityUtils::getCurrentUserId);
        assertThrows(IllegalStateException.class, SecurityUtils::getCurrentUserEmail);
    }
}
