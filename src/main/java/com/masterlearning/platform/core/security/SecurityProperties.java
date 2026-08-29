package com.masterlearning.platform.core.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
    int bcryptStrength,
    Cors cors
) {
    public record Cors(String[] allowedOrigins) {}
}