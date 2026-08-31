package com.masterlearning.platform.modules.identity.mapper;

import com.masterlearning.platform.modules.identity.dto.response.RoleResponse;
import com.masterlearning.platform.modules.identity.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(
            target = "permissions",
            expression = "java(role.getPermissions().stream().map(permission -> permission.getCode()).collect(java.util.stream.Collectors.toUnmodifiableSet()))"
    )
    RoleResponse toResponse(Role role);
}