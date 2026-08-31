package com.masterlearning.platform.modules.user.service.impl;

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
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final UserMapper mapper;

    public UserManagementServiceImpl(UserRepository users, RoleRepository roles, UserMapper mapper) {
        this.users = users;
        this.roles = roles;
        this.mapper = mapper;
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public List<UserResponse> getAll(String query, Boolean enabled) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);

        return users.findAllBy().stream()
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
    @Transactional(Transactional.TxType.REQUIRED)
    public UserResponse getById(UUID id) {
        return mapper.toResponse(loadUser(id));
    }

    @Override
    public UserResponse updateStatus(UUID id, UpdateUserStatusRequest request) {
        User user = loadUser(id);
        if (request.enabled()) user.enable();
        else user.disable();
        return mapper.toResponse(users.save(user));
    }

    @Override
    public UserResponse updateRoles(UUID id, UpdateUserRolesRequest request) {
        User user = loadUser(id);
        Set<Role> resolvedRoles = request.roles().stream()
                .map(code -> roles.findByCode(code.trim().toUpperCase(Locale.ROOT))
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + code)))
                .collect(Collectors.toSet());

        user.replaceRoles(resolvedRoles);
        return mapper.toResponse(users.save(user));
    }

    private User loadUser(UUID id) {
        return users.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
