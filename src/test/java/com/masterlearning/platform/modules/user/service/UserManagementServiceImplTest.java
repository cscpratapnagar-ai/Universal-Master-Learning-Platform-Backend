package com.masterlearning.platform.modules.user.service;

import com.masterlearning.platform.common.exception.ForbiddenException;
import com.masterlearning.platform.common.exception.ResourceNotFoundException;
import com.masterlearning.platform.modules.identity.entity.Role;
import com.masterlearning.platform.modules.identity.repository.RoleRepository;
import com.masterlearning.platform.modules.user.dto.request.UpdateUserRolesRequest;
import com.masterlearning.platform.modules.user.dto.request.UpdateUserStatusRequest;
import com.masterlearning.platform.modules.user.entity.User;
import com.masterlearning.platform.modules.user.dto.response.UserResponse;
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
    void rejectsReadingUnknownUserWithoutMappingResponse() {
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(id));

        verifyNoInteractions(mapper);
    }

    @Test
    void rejectsUpdatingStatusForUnknownUserWithoutSaving() {
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateStatus(id, new UpdateUserStatusRequest(false)));

        verify(users, never()).save(any());
        verifyNoInteractions(roles);
    }

    @Test
    void rejectsUpdatingRolesForUnknownUserWithoutResolvingRoles() {
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateRoles(id, new UpdateUserRolesRequest(Set.of("LEARNER"))));

        verifyNoInteractions(roles);
        verify(users, never()).save(any());
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
    void allowsDisablingSuperAdminWhenAnotherActiveSuperAdminRemains() {
        User target = new User("other-admin@example.com", "hash", "Other", "Admin");
        Role superAdmin = mock(Role.class);
        when(superAdmin.getCode()).thenReturn("SUPER_ADMIN");
        target.assignRole(superAdmin);
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.of(target));
        when(roles.findByCodeForUpdate("SUPER_ADMIN")).thenReturn(Optional.of(superAdmin));
        when(users.countActiveSuperAdmins()).thenReturn(2L);
        when(users.save(target)).thenReturn(target);

        service.updateStatus(id, new UpdateUserStatusRequest(false));

        assertFalse(target.isEnabled());
        verify(roles).findByCodeForUpdate("SUPER_ADMIN");
        verify(users).countActiveSuperAdmins();
        verify(users).save(target);
    }

    @Test
    void preventsDisablingTheLastActiveSuperAdmin() {
        User target = new User("other@example.com", "hash", "Other", "Admin");
        Role superAdmin = mock(Role.class);
        when(superAdmin.getCode()).thenReturn("SUPER_ADMIN");
        target.assignRole(superAdmin);
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.of(target));
        when(roles.findByCodeForUpdate("SUPER_ADMIN")).thenReturn(Optional.of(superAdmin));
        when(users.countActiveSuperAdmins()).thenReturn(1L);

        assertThrows(ForbiddenException.class,
                () -> service.updateStatus(id, new UpdateUserStatusRequest(false)));

        assertTrue(target.isEnabled());
        verify(users, never()).save(any());
    }

    @Test
    void rejectsRemovingProtectedSuperAdminRoleEvenWhenAnotherActiveAdminRemains() {
        User target = new User("other-admin@example.com", "hash", "Other", "Admin");
        Role superAdmin = mock(Role.class);
        when(superAdmin.getCode()).thenReturn("SUPER_ADMIN");
        target.assignRole(superAdmin);
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.of(target));

        assertThrows(ForbiddenException.class,
                () -> service.updateRoles(id, new UpdateUserRolesRequest(Set.of("LEARNER"))));

        assertTrue(target.getRoles().stream()
                .anyMatch(role -> "SUPER_ADMIN".equals(role.getCode())));
        verify(roles, never()).findByCodeForUpdate("SUPER_ADMIN");
        verify(users, never()).countActiveSuperAdmins();
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


    @Test
    void rejectsProtectedSuperAdminRoleRemovalBeforeLastAdminCountCheck() {
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
        verify(roles, never()).findByCodeForUpdate("SUPER_ADMIN");
        verify(users, never()).countActiveSuperAdmins();
        verify(users, never()).save(any());
    }


    @Test
    void disablingRegularUserDoesNotAcquireSuperAdminLock() {
        User target = new User("learner@example.com", "hash", "Learner", "User");
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.of(target));
        when(users.save(target)).thenReturn(target);

        service.updateStatus(id, new UpdateUserStatusRequest(false));

        assertFalse(target.isEnabled());
        verify(roles, never()).findByCodeForUpdate("SUPER_ADMIN");
        verify(users, never()).countActiveSuperAdmins();
        verify(users).save(target);
    }

    @Test
    void disablingAlreadyDisabledSuperAdminDoesNotRunLastAdminGuard() {
        User target = new User("disabled-admin@example.com", "hash", "Disabled", "Admin");
        target.disable();
        Role superAdmin = mock(Role.class);
        when(superAdmin.getCode()).thenReturn("SUPER_ADMIN");
        target.assignRole(superAdmin);
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.of(target));
        when(users.save(target)).thenReturn(target);

        service.updateStatus(id, new UpdateUserStatusRequest(false));

        assertFalse(target.isEnabled());
        verify(roles, never()).findByCodeForUpdate("SUPER_ADMIN");
        verify(users, never()).countActiveSuperAdmins();
        verify(users).save(target);
    }

    @Test
    void enablingAlreadyEnabledRegularUserKeepsAccountEnabled() {
        User target = new User("learner@example.com", "hash", "Learner", "User");
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.of(target));
        when(users.save(target)).thenReturn(target);

        service.updateStatus(id, new UpdateUserStatusRequest(true));

        assertTrue(target.isEnabled());
        verify(users).save(target);
        verify(roles, never()).findByCodeForUpdate("SUPER_ADMIN");
        verify(users, never()).countActiveSuperAdmins();
    }

    @Test
    void allowsEnablingAnotherUsersDisabledAccount() {
        User target = new User("learner@example.com", "hash", "Learner", "User");
        target.disable();
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.of(target));
        when(users.save(target)).thenReturn(target);

        service.updateStatus(id, new UpdateUserStatusRequest(true));

        assertTrue(target.isEnabled());
        verify(users).save(target);
    }

    @Test
    void updatingUserWithSameAssignableRolePreservesRoleSet() {
        User target = new User("learner@example.com", "hash", "Learner", "User");
        Role learner = mock(Role.class);
        when(learner.getCode()).thenReturn("LEARNER");
        target.assignRole(learner);
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.of(target));
        when(roles.findByCode("LEARNER")).thenReturn(Optional.of(learner));
        when(users.save(target)).thenReturn(target);

        service.updateRoles(id, new UpdateUserRolesRequest(Set.of("LEARNER")));

        assertEquals(1, target.getRoles().size());
        assertTrue(target.getRoles().contains(learner));
        verify(users).save(target);
    }

    @Test
    void collapsesRoleCodesThatNormalizeToTheSameValue() {
        User target = new User("learner@example.com", "hash", "Learner", "User");
        Role learner = mock(Role.class);
        when(learner.getCode()).thenReturn("LEARNER");
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.of(target));
        when(roles.findByCode("LEARNER")).thenReturn(Optional.of(learner));
        when(users.save(target)).thenReturn(target);

        service.updateRoles(id,
                new UpdateUserRolesRequest(Set.of("LEARNER", " learner ")));

        assertEquals(1, target.getRoles().size());
        assertTrue(target.getRoles().contains(learner));
        verify(roles, times(2)).findByCode("LEARNER");
        verify(users).save(target);
    }

    @Test
    void normalizesAssignableRoleCodesBeforeResolvingThem() {
        User target = new User("learner@example.com", "hash", "Learner", "User");
        Role learner = mock(Role.class);
        when(learner.getCode()).thenReturn("LEARNER");
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.of(target));
        when(roles.findByCode("LEARNER")).thenReturn(Optional.of(learner));
        when(users.save(target)).thenReturn(target);

        service.updateRoles(id, new UpdateUserRolesRequest(Set.of(" learner ")));

        assertTrue(target.getRoles().stream().anyMatch(role -> "LEARNER".equals(role.getCode())));
        verify(roles).findByCode("LEARNER");
        verify(users).save(target);
    }


    @Test
    void returnsAllUsersWhenNoQueryOrStatusFilterIsProvided() {
        User first = new User("first@example.com", "hash", "First", "User");
        User second = new User("second@example.com", "hash", "Second", "User");
        second.disable();
        when(users.findAllWithRoles()).thenReturn(java.util.List.of(first, second));
        when(mapper.toResponse(first)).thenReturn(new UserResponse(
                UUID.randomUUID(), first.getEmail(), first.getFirstName(),
                first.getLastName(), true, Set.of("LEARNER")));
        when(mapper.toResponse(second)).thenReturn(new UserResponse(
                UUID.randomUUID(), second.getEmail(), second.getFirstName(),
                second.getLastName(), false, Set.of("LEARNER")));

        var result = service.getAll(null, null);

        assertEquals(2, result.size());
        verify(mapper).toResponse(first);
        verify(mapper).toResponse(second);
    }

    @Test
    void filtersUsersByDisabledStatusWithoutQuery() {
        User enabledUser = new User("enabled@example.com", "hash", "Enabled", "User");
        User disabledUser = new User("disabled@example.com", "hash", "Disabled", "User");
        disabledUser.disable();
        when(users.findAllWithRoles()).thenReturn(java.util.List.of(enabledUser, disabledUser));
        when(mapper.toResponse(disabledUser)).thenReturn(new UserResponse(
                UUID.randomUUID(), disabledUser.getEmail(), disabledUser.getFirstName(),
                disabledUser.getLastName(), false, Set.of("LEARNER")));

        var result = service.getAll(null, false);

        assertEquals(1, result.size());
        assertEquals("disabled@example.com", result.get(0).email());
        verify(mapper).toResponse(disabledUser);
        verify(mapper, never()).toResponse(enabledUser);
    }

    @Test
    void filtersUsersByEnabledStatusWithoutQuery() {
        User enabledUser = new User("enabled@example.com", "hash", "Enabled", "User");
        User disabledUser = new User("disabled@example.com", "hash", "Disabled", "User");
        disabledUser.disable();
        when(users.findAllWithRoles()).thenReturn(java.util.List.of(enabledUser, disabledUser));
        when(mapper.toResponse(enabledUser)).thenReturn(new UserResponse(
                UUID.randomUUID(), enabledUser.getEmail(), enabledUser.getFirstName(),
                enabledUser.getLastName(), true, Set.of("LEARNER")));

        var result = service.getAll(null, true);

        assertEquals(1, result.size());
        assertEquals("enabled@example.com", result.get(0).email());
        verify(mapper).toResponse(enabledUser);
        verify(mapper, never()).toResponse(disabledUser);
    }

    @Test
    void filtersUsersByEmailAndLastNameCaseInsensitively() {
        User emailMatch = new User("special.teacher@example.com", "hash", "Asha", "Patel");
        User lastNameMatch = new User("another@example.com", "hash", "Ravi", "Mehta");
        User unrelated = new User("learner@example.com", "hash", "Nita", "Desai");
        when(users.findAllWithRoles()).thenReturn(
                java.util.List.of(emailMatch, lastNameMatch, unrelated));
        when(mapper.toResponse(emailMatch)).thenReturn(new UserResponse(
                UUID.randomUUID(), emailMatch.getEmail(), emailMatch.getFirstName(),
                emailMatch.getLastName(), true, Set.of("TEACHER")));
        when(mapper.toResponse(lastNameMatch)).thenReturn(new UserResponse(
                UUID.randomUUID(), lastNameMatch.getEmail(), lastNameMatch.getFirstName(),
                lastNameMatch.getLastName(), true, Set.of("LEARNER")));

        var emailResults = service.getAll("SPECIAL.TEACHER", null);
        var lastNameResults = service.getAll("MEHTA", null);

        assertEquals(1, emailResults.size());
        assertEquals("special.teacher@example.com", emailResults.get(0).email());
        assertEquals(1, lastNameResults.size());
        assertEquals("another@example.com", lastNameResults.get(0).email());
        verify(mapper, never()).toResponse(unrelated);
    }

    @Test
    void filtersUsersByCaseInsensitiveQueryAndEnabledStatus() {
        User enabledMatch = new User("teacher@example.com", "hash", "Mira", "Patel");
        User disabledMatch = new User("former@example.com", "hash", "Mira", "Shah");
        disabledMatch.disable();
        User unrelated = new User("learner@example.com", "hash", "Ravi", "Desai");
        when(users.findAllWithRoles()).thenReturn(java.util.List.of(enabledMatch, disabledMatch, unrelated));

        UserResponse enabledResponse = new UserResponse(
                UUID.randomUUID(), enabledMatch.getEmail(), enabledMatch.getFirstName(),
                enabledMatch.getLastName(), true, Set.of("TEACHER"));
        when(mapper.toResponse(enabledMatch)).thenReturn(enabledResponse);

        var result = service.getAll("  MIRA  ", true);

        assertEquals(1, result.size());
        assertEquals("teacher@example.com", result.get(0).email());
        verify(mapper).toResponse(enabledMatch);
        verify(mapper, never()).toResponse(disabledMatch);
        verify(mapper, never()).toResponse(unrelated);
    }


    @Test
    void rejectsMixedValidAndUnknownRolesWithoutPartiallyUpdatingUser() {
        User target = new User("learner@example.com", "hash", "Learner", "User");
        Role learner = mock(Role.class);
        lenient().when(learner.getCode()).thenReturn("LEARNER");
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.of(target));
        lenient().when(roles.findByCode("LEARNER")).thenReturn(Optional.of(learner));
        lenient().when(roles.findByCode("UNKNOWN_ROLE")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateRoles(id,
                        new UpdateUserRolesRequest(Set.of("LEARNER", "UNKNOWN_ROLE"))));

        assertTrue(target.getRoles().isEmpty());
        verify(users, never()).save(any());
    }

    @Test
    void rejectsUnknownRoleCodeWithoutSavingUser() {
        User target = new User("learner@example.com", "hash", "Learner", "User");
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.of(target));
        when(roles.findByCode("UNKNOWN_ROLE")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateRoles(id, new UpdateUserRolesRequest(Set.of("unknown_role"))));

        assertTrue(target.getRoles().isEmpty());
        verify(users, never()).save(any());
    }

}
