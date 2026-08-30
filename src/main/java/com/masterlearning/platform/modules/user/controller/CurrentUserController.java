package com.masterlearning.platform.modules.user.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.user.dto.response.UserResponse;
import com.masterlearning.platform.modules.user.mapper.UserMapper;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class CurrentUserController {

    private final UserRepository users;
    private final UserMapper mapper;

    public CurrentUserController(UserRepository users, UserMapper mapper) {
        this.users = users;
        this.mapper = mapper;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        return users.findById(SecurityUtils.getCurrentUserId())
                .map(mapper::toResponse)
                .map(user -> ApiResponse.success("Current user loaded", user))
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
    }
}
