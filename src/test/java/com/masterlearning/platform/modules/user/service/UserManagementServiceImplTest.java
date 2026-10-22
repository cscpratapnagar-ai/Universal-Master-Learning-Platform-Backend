package com.masterlearning.platform.modules.user.service;

import com.masterlearning.platform.common.exception.ForbiddenException;
import com.masterlearning.platform.modules.identity.entity.Role;
import com.masterlearning.platform.modules.identity.repository.RoleRepository;
import com.masterlearning.platform.modules.user.dto.request.UpdateUserRolesRequest;
import com.masterlearning.platform.modules.user.dto.request.UpdateUserStatusRequest;
import com.masterlearning.platform.modules.user.entity.User;
import com.masterlearning.platform.modules.user.mapper.UserMapper;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.modules.user.service.impl.UserManagementServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceImplTest {

    @Mock private UserRepository users;
    @Mock private RoleRepository roles;
    @Mock private UserMapper mapper;

    private UserManagementServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserManagementServiceImpl(users, roles, mapper);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@example.com", "test"));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsChangingOwnAccountStatus() {
        User currentUser = new User("admin@example.com", "hash", "Admin", "User");
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.of(currentUser));

        assertThrows(ForbiddenException.class,
                () -> service.updateStatus(id, new UpdateUserStatusRequest(false)));

        verify(users, never()).save(any());
    }

    @Test
    void rejectsChangingOwnRoles() {
        User currentUser = new User("admin@example.com", "hash", "Admin", "User");
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.of(currentUser));

        assertThrows(ForbiddenException.class,
                () -> service.updateRoles(id, new UpdateUserRolesRequest(Set.of("LEARNER"))));

        verifyNoInteractions(roles);
        verify(users, never()).save(any());
    }

    @Test
    void preventsDisablingTheLastActiveSuperAdmin() {
        User target = new User("other@example.com", "hash", "Other", "Admin");
        Role superAdmin = mock(Role.class);
        when(superAdmin.getCode()).thenReturn("SUPER_ADMIN");
        target.assignRole(superAdmin);
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.of(target));
        when(users.countActiveSuperAdmins()).thenReturn(1L);

        assertThrows(ForbiddenException.class,
                () -> service.updateStatus(id, new UpdateUserStatusRequest(false)));

        assertTrue(target.isEnabled());
        verify(users, never()).save(any());
    }

    @Test
    void preventsRemovingProtectedSuperAdminRoleThroughUserManagement() {
        User target = new User("other@example.com", "hash", "Other", "Admin");
        Role superAdmin = mock(Role.class);
        Role learner = mock(Role.class);
        when(superAdmin.getCode()).thenReturn("SUPER_ADMIN");
        when(learner.getCode()).thenReturn("LEARNER");
        target.assignRole(superAdmin);
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.of(target));
        when(roles.findByCode("LEARNER")).thenReturn(Optional.of(learner));

        assertThrows(ForbiddenException.class,
                () -> service.updateRoles(id, new UpdateUserRolesRequest(Set.of("LEARNER"))));

        assertTrue(target.getRoles().stream().anyMatch(role -> "SUPER_ADMIN".equals(role.getCode())));
        verify(users, never()).save(any());
    }

    @Test
    void preventsGrantingProtectedSuperAdminRoleThroughUserManagement() {
        User target = new User("other@example.com", "hash", "Other", "User");
        Role learner = mock(Role.class);
        Role superAdmin = mock(Role.class);
        when(learner.getCode()).thenReturn("LEARNER");
        when(superAdmin.getCode()).thenReturn("SUPER_ADMIN");
        target.assignRole(learner);
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.of(target));
        when(roles.findByCode("SUPER_ADMIN")).thenReturn(Optional.of(superAdmin));

        assertThrows(ForbiddenException.class,
                () -> service.updateRoles(id, new UpdateUserRolesRequest(Set.of("SUPER_ADMIN"))));

        verify(users, never()).save(any());
    }

    @Test
    void preventsRemovingExistingProtectedStudentRoleThroughUserManagement() {
        User target = new User("other@example.com", "hash", "Other", "User");
        Role student = mock(Role.class);
        Role learner = mock(Role.class);
        when(student.getCode()).thenReturn("STUDENT");
        when(learner.getCode()).thenReturn("LEARNER");
        target.assignRole(student);
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.of(target));
        when(roles.findByCode("LEARNER")).thenReturn(Optional.of(learner));

        assertThrows(ForbiddenException.class,
                () -> service.updateRoles(id, new UpdateUserRolesRequest(Set.of("LEARNER"))));

        assertTrue(target.getRoles().stream().anyMatch(role -> "STUDENT".equals(role.getCode())));
        verify(users, never()).save(any());
    }

}
