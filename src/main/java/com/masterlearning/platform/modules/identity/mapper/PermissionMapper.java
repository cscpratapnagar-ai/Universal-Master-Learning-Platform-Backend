package com.masterlearning.platform.modules.identity.mapper;

import com.masterlearning.platform.modules.identity.dto.response.PermissionResponse;
import com.masterlearning.platform.modules.identity.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionResponse toResponse(Permission permission);
}