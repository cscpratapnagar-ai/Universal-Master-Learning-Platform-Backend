package com.masterlearning.platform.modules.user.mapper;

import com.masterlearning.platform.modules.user.dto.response.UserResponse;
import com.masterlearning.platform.modules.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roles", expression = "java(user.getRoles().stream().map(role -> role.getCode()).collect(java.util.stream.Collectors.toUnmodifiableSet()))")
    UserResponse toResponse(User user);
}