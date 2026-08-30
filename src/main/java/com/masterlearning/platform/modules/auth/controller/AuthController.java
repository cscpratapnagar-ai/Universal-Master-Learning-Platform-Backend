package com.masterlearning.platform.modules.auth.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.auth.dto.request.*;
import com.masterlearning.platform.modules.auth.dto.response.AuthResponse;
import com.masterlearning.platform.modules.auth.service.AuthService;
import com.masterlearning.platform.modules.user.dto.response.UserResponse;
import com.masterlearning.platform.modules.user.entity.User;
import com.masterlearning.platform.modules.user.mapper.UserMapper;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.security.authority.CurrentUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService auth;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public AuthController(
            AuthService auth,
            UserRepository userRepository,
            UserMapper userMapper
    ) {
        this.auth = auth;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("Registration successful", auth.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Login successful", auth.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success("Token refreshed", auth.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        auth.logout(request);
        return ApiResponse.success("Logout successful", null);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(
            @AuthenticationPrincipal CurrentUserPrincipal principal
    ) {
        User user = userRepository.findWithAuthoritiesById(principal.userId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));

        return ApiResponse.success("Current user retrieved", userMapper.toResponse(user));
    }
}