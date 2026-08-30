package com.masterlearning.platform.security.authority;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthorizationServiceTest {

    private final AuthorizationService authorizationService = new AuthorizationService();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void currentUserId_returnsAuthenticatedPrincipalId() {
        UUID userId = UUID.randomUUID();
        authenticate(userId, List.of("COURSE_READ"));

        assertThat(authorizationService.currentUserId()).isEqualTo(userId);
    }

    @Test
    void currentUserId_rejectsMissingAuthentication() {
        assertThatThrownBy(authorizationService::currentUserId)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void isCurrentUser_allowsOnlyResourceOwner() {
        UUID userId = UUID.randomUUID();
        authenticate(userId, List.of());

        assertThat(authorizationService.isCurrentUser(userId)).isTrue();
        assertThat(authorizationService.isCurrentUser(UUID.randomUUID())).isFalse();
    }

    @Test
    void authorityChecks_areExactAndDoNotOverGrant() {
        authenticate(UUID.randomUUID(), List.of("COURSE_READ", "COURSE_CREATE"));

        assertThat(authorizationService.hasAuthority("COURSE_READ")).isTrue();
        assertThat(authorizationService.hasAuthority("COURSE_DELETE")).isFalse();
        assertThat(authorizationService.hasAnyAuthority("USER_READ", "COURSE_CREATE")).isTrue();
        assertThat(authorizationService.hasAnyAuthority("USER_READ", "COURSE_DELETE")).isFalse();
    }

    private void authenticate(UUID userId, List<String> authorities) {
        CurrentUserPrincipal principal = new CurrentUserPrincipal(userId, "test@example.com");
        var grantedAuthorities = authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, grantedAuthorities)
        );
    }
}
