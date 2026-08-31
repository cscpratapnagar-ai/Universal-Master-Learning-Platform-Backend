package com.masterlearning.platform.modules.user.mapper;

import com.masterlearning.platform.modules.user.dto.response.UserResponse;
import com.masterlearning.platform.modules.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;

        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getCode())
                .collect(Collectors.toUnmodifiableSet());

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isEnabled(),
                roles
        );
    }
}