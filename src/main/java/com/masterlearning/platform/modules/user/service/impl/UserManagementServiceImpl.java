package com.masterlearning.platform.modules.user.service.impl;

import com.masterlearning.platform.common.exception.ForbiddenException;
import com.masterlearning.platform.common.exception.ResourceNotFoundException;
import com.masterlearning.platform.modules.identity.entity.Role;
import com.masterlearning.platform.modules.identity.repository.RoleRepository;
import com.masterlearning.platform.modules.user.dto.request.UpdateUserRolesRequest;
import com.masterlearning.platform.modules.user.dto.request.UpdateUserStatusRequest;
import com.masterlearning.platform.modules.user.dto.response.UserResponse;
import com.masterlearning.platform.modules.user.entity.User;
import com.masterlearning.platform.modules.user.mapper.UserMapper;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.modules.user.service.UserManagementService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserManagementServiceImpl implements UserManagementService {

    private static final String SUPER_ADMIN = "SUPER_ADMIN";

    private final UserRepository users;
    private final RoleRepository roles;
    private final UserMapper mapper;

    public UserManagementServiceImpl(UserRepository users, RoleRepository roles, UserMapper mapper) {
        this.users = users;
        this.roles = roles;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAll(String query, Boolean enabled) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);

        return users.findAllWithRoles().stream()
                .filter(user -> enabled == null || user.isEnabled() == enabled)
                .filter(user -> matchesQuery(user, normalized))
                .map(mapper::toResponse)
                .toList();
    }

    private boolean matchesQuery(User user, String normalized) {
        if (normalized.isBlank()) {
            return true;
        }

        return containsIgnoreCase(user.getEmail(), normalized)
                || containsIgnoreCase(user.getFirstName(), normalized)
                || containsIgnoreCase(user.getLastName(), normalized);
    }

    private boolean containsIgnoreCase(String value, String normalized) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalized);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return mapper.toResponse(loadUser(id));
    }

    @Override
    public UserResponse updateStatus(UUID id, UpdateUserStatusRequest request) {
        User user = loadUser(id);
        assertNotSelf(user);

        if (!request.enabled()) {
            assertNotLastActiveSuperAdmin(user, userHasRole(user, SUPER_ADMIN));
            user.disable();
        } else {
            user.enable();
        }

        return mapper.toResponse(users.save(user));
    }

    @Override
    public UserResponse updateRoles(UUID id, UpdateUserRolesRequest request) {
        User user = loadUser(id);
        assertNotSelf(user);

        Set<Role> resolvedRoles = request.roles().stream()
                .map(code -> roles.findByCode(code.trim().toUpperCase(Locale.ROOT))
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + code)))
                .collect(Collectors.toSet());

        boolean currentlySuperAdmin = userHasRole(user, SUPER_ADMIN);
        boolean willRemainSuperAdmin = resolvedRoles.stream()
                .anyMatch(role -> SUPER_ADMIN.equalsIgnoreCase(role.getCode()));

        if (currentlySuperAdmin && !willRemainSuperAdmin) {
            assertNotLastActiveSuperAdmin(user, true);
        }

        user.replaceRoles(resolvedRoles);
        return mapper.toResponse(users.save(user));
    }

    private void assertNotSelf(User targetUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new ForbiddenException("Authenticated user context is required");
        }

        String actorEmail = authentication.getName();

        if (actorEmail.equalsIgnoreCase(targetUser.getEmail())) {
            throw new ForbiddenException(
                    "You cannot modify your own roles or account status from user management"
            );
        }
    }

    private void assertNotLastActiveSuperAdmin(User user, boolean affectsSuperAdminAccess) {
        if (!affectsSuperAdminAccess || !user.isEnabled()) {
            return;
        }

        if (users.countActiveSuperAdmins() <= 1) {
            throw new ForbiddenException(
                    "The last active SUPER_ADMIN account cannot be disabled or stripped of SUPER_ADMIN access"
            );
        }
    }

    private boolean userHasRole(User user, String roleCode) {
        return user.getRoles().stream()
                .anyMatch(role -> roleCode.equalsIgnoreCase(role.getCode()));
    }

    private User loadUser(UUID id) {
        return users.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
