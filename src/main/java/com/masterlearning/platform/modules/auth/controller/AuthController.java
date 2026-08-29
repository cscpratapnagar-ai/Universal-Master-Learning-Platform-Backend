package com.masterlearning.platform.modules.auth.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.auth.dto.request.*;
import com.masterlearning.platform.modules.auth.dto.response.AuthResponse;
import com.masterlearning.platform.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth){this.auth=auth;}
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest r){return ApiResponse.success("Registration successful",auth.register(r));}
    @PostMapping("/login") public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest r){return ApiResponse.success("Login successful",auth.login(r));}
    @PostMapping("/refresh") public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest r){return ApiResponse.success("Token refreshed",auth.refresh(r));}
    @PostMapping("/logout") public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest r){auth.logout(r);return ApiResponse.success("Logout successful",null);}
}