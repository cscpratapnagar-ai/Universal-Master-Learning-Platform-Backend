package com.masterlearning.platform.modules.auth.service;
import com.masterlearning.platform.modules.auth.dto.request.*;
import com.masterlearning.platform.modules.auth.dto.response.AuthResponse;
public interface AuthService { AuthResponse register(RegisterRequest request); AuthResponse login(LoginRequest request); AuthResponse refresh(RefreshTokenRequest request); void logout(LogoutRequest request); }