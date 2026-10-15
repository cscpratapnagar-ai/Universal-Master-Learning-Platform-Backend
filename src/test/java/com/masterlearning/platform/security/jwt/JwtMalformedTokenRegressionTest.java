package com.masterlearning.platform.security.jwt;

import com.masterlearning.platform.security.config.SecurityProperties;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtMalformedTokenRegressionTest {
    @Test
    void rejectsMalformedCompactToken() {
        var properties = new SecurityProperties(
                12,
                new SecurityProperties.Jwt(
                        "this-is-a-test-secret-key-that-is-long-enough-for-hmac-sha256",
                        15,
                        "trusted-issuer"
                ),
                new SecurityProperties.Cors(new String[]{"http://localhost"})
        );
        var service = new JwtService(properties);

        assertThrows(JwtException.class, () -> service.extractUserId("not.a.valid.jwt"));
    }
}
