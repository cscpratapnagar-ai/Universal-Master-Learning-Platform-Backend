package com.masterlearning.platform.modules.auth.dto.response;
import com.masterlearning.platform.modules.user.dto.response.UserResponse;
public record AuthResponse(String accessToken,String refreshToken,String tokenType,UserResponse user){}