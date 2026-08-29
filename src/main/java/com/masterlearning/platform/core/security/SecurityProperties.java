package com.masterlearning.platform.core.security;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix="app.security")
public record SecurityProperties(int bcryptStrength, Jwt jwt, Login login, Cors cors){
 public record Jwt(String secret,int accessTokenMinutes,int refreshTokenDays){}
 public record Login(int maxAttempts,int lockMinutes){}
 public record Cors(String[] allowedOrigins){}
}