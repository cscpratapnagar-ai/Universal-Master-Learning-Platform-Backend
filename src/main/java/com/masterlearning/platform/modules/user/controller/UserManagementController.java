package com.masterlearning.platform.modules.user.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.user.dto.request.UpdateUserRolesRequest;
import com.masterlearning.platform.modules.user.dto.request.UpdateUserStatusRequest;
import com.masterlearning.platform.modules.user.dto.response.UserResponse;
import com.masterlearning.platform.modules.user.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class UserManagementController {

    private final UserManagementService service;

    public UserManagementController(UserManagementService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getAll(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean enabled
    ) {
        return ApiResponse.success("Users retrieved successfully", service.getAll(query, enabled));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success("User retrieved successfully", service.getById(id));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<UserResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return ApiResponse.success("User status updated successfully", service.updateStatus(id, request));
    }

    @PutMapping("/{id}/roles")
    public ApiResponse<UserResponse> updateRoles(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRolesRequest request
    ) {
        return ApiResponse.success("User roles updated successfully", service.updateRoles(id, request));
    }
}