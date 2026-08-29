package com.masterlearning.platform.core.auth;
import jakarta.validation.constraints.*; import java.util.UUID;
public final class AuthDtos {
 private AuthDtos(){}
 public record RegisterRequest(@NotBlank @Email String email,@NotBlank @Size(min=12,max=128) String password,@NotBlank @Size(max=100) String firstName,@Size(max=100) String lastName){}
 public record LoginRequest(@NotBlank @Email String email,@NotBlank String password){}
 public record RefreshRequest(@NotBlank String refreshToken){}
 public record AuthResponse(String accessToken,String refreshToken,String tokenType,long expiresIn,UUID userId,String email){}
}