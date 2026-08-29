package com.masterlearning.platform.modules.identity.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.identity.dto.response.PermissionResponse;
import com.masterlearning.platform.modules.identity.dto.response.RoleResponse;
import com.masterlearning.platform.modules.identity.service.PermissionService;
import com.masterlearning.platform.modules.identity.service.RoleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/identity")
public class IdentityController {

    private final RoleService roleService;
    private final PermissionService permissionService;

    public IdentityController(
            RoleService roleService,
            PermissionService permissionService
    ) {
        this.roleService = roleService;
        this.permissionService = permissionService;
    }

    @GetMapping("/roles")
    public ApiResponse<List<RoleResponse>> getRoles() {
        return ApiResponse.success(
                "Roles retrieved successfully",
                roleService.getAll()
        );
    }

    @GetMapping("/permissions")
    public ApiResponse<List<PermissionResponse>> getPermissions() {
        return ApiResponse.success(
                "Permissions retrieved successfully",
                permissionService.getAll()
        );
    }
}