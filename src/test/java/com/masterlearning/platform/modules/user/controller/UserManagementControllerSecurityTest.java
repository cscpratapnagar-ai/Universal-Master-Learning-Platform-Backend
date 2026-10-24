package com.masterlearning.platform.modules.user.controller;

import com.masterlearning.platform.modules.user.service.UserManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(classes = UserManagementControllerSecurityTest.TestConfiguration.class)
class UserManagementControllerSecurityTest {

    @Autowired
    private UserManagementController controller;

    @Autowired
    private UserManagementService service;

    @Test
    void deniesUnauthenticatedInvocationOfProtectedUserManagementMethod() {
        assertThrows(AccessDeniedException.class, () -> controller.getAll(null, null));
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void deniesAuthenticatedNonSuperAdminFromReadingUsers() {
        assertThrows(AccessDeniedException.class, () -> controller.getAll(null, null));
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "SUPER_ADMIN")
    void allowsSuperAdminToInvokeUserManagementMethod() {
        controller.getById(UUID.randomUUID());
        verify(service).getById(any(UUID.class));
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfiguration {
        @Bean
        UserManagementService userManagementService() {
            return mock(UserManagementService.class);
        }

        @Bean
        UserManagementController userManagementController(UserManagementService service) {
            return new UserManagementController(service);
        }
    }
}
