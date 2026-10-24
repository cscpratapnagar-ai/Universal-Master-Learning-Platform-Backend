package com.masterlearning.platform.modules.user.service;

import com.masterlearning.platform.modules.user.dto.request.CreateUserRequest;
import com.masterlearning.platform.modules.user.dto.request.UpdateUserRolesRequest;
import com.masterlearning.platform.modules.user.dto.request.UpdateUserStatusRequest;
import com.masterlearning.platform.modules.user.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserManagementService {
    UserResponse createUser(CreateUserRequest request);
    List<UserResponse> getAll(String query, Boolean enabled);
    UserResponse getById(UUID id);
    UserResponse updateStatus(UUID id, UpdateUserStatusRequest request);
    UserResponse updateRoles(UUID id, UpdateUserRolesRequest request);
}