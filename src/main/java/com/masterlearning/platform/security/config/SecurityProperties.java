package com.masterlearning.platform.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        int bcryptStrength,
        Jwt jwt,
        Cors cors
) {
    public record Jwt(
            String secret,
            int accessTokenMinutes,
            String issuer
    ) {}

    public record Cors(
            String[] allowedOrigins
    ) {}
}