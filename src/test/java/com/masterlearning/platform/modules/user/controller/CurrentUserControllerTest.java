package com.masterlearning.platform.modules.user.controller;

import com.masterlearning.platform.modules.user.entity.User;
import com.masterlearning.platform.modules.user.dto.response.UserResponse;
import com.masterlearning.platform.modules.user.mapper.UserMapper;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.security.authority.CurrentUserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrentUserControllerTest {

    private final UserRepository users = mock(UserRepository.class);
    private final UserMapper mapper = mock(UserMapper.class);
    private final CurrentUserController controller = new CurrentUserController(users, mapper);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsMappedCurrentUserForAuthenticatedPrincipal() {
        UUID userId = UUID.randomUUID();
        var principal = new CurrentUserPrincipal(userId, "learner@example.com");
        User user = mock(User.class);
        UserResponse response = new UserResponse(
                userId, "learner@example.com", "Asha", "Patel", true, java.util.Set.of("LEARNER"));
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of()));
        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(mapper.toResponse(user)).thenReturn(response);

        var result = controller.me();

        assertTrue(result.success());
        assertEquals("Current user loaded", result.message());
        assertEquals(response, result.data());
    }

    @Test
    void failsClearlyWhenAuthenticatedUserRecordNoLongerExists() {
        UUID userId = UUID.randomUUID();
        var principal = new CurrentUserPrincipal(userId, "learner@example.com");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of()));
        when(users.findById(userId)).thenReturn(Optional.empty());

        IllegalStateException error = assertThrows(
                IllegalStateException.class, controller::me);

        assertEquals("Authenticated user no longer exists", error.getMessage());
    }
}
