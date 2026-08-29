package com.masterlearning.platform.security.jwt;

import com.masterlearning.platform.security.config.SecurityProperties;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtServiceTests {

    @Test
    void shouldGenerateAndReadAccessToken() {
        SecurityProperties properties = new SecurityProperties(
                12,
                new SecurityProperties.Jwt(
                        "this-is-a-test-secret-key-that-is-long-enough-for-hmac-sha256",
                        15,
                        "test-issuer"
                ),
                new SecurityProperties.Cors(new String[]{"http://localhost"})
        );

        JwtService service = new JwtService(properties);
        UUID userId = UUID.randomUUID();

        String token = service.generateAccessToken(
                userId,
                "student@example.com"
        );

        assertEquals(userId, service.extractUserId(token));
    }
}