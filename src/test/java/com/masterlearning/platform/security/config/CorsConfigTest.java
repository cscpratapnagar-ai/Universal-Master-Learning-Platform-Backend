package com.masterlearning.platform.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsConfigTest {
    @Test
    void allowsConfiguredFrontendOrigin() {
        var properties = new SecurityProperties(
                12,
                new SecurityProperties.Jwt(
                        "this-is-a-test-secret-key-that-is-long-enough-for-hmac-sha256",
                        15,
                        "trusted-issuer"
                ),
                new SecurityProperties.Cors(new String[]{"https://learning.example.com"})
        );
        var source = new CorsConfig().corsConfigurationSource(properties);
        CorsConfiguration config = source.getCorsConfiguration(
                new MockHttpServletRequest("GET", "/api/v1/learner/courses"));

        assertTrue(config.checkOrigin("https://learning.example.com") != null);
    }

    @Test
    void rejectsUnconfiguredFrontendOrigin() {
        var properties = new SecurityProperties(
                12,
                new SecurityProperties.Jwt(
                        "this-is-a-test-secret-key-that-is-long-enough-for-hmac-sha256",
                        15,
                        "trusted-issuer"
                ),
                new SecurityProperties.Cors(new String[]{"https://learning.example.com"})
        );
        var source = new CorsConfig().corsConfigurationSource(properties);
        CorsConfiguration config = source.getCorsConfiguration(
                new MockHttpServletRequest("GET", "/api/v1/learner/courses"));

        assertFalse(config.checkOrigin("https://attacker.example") != null);
    }
}
