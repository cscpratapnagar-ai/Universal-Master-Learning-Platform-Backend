package com.masterlearning.platform.security.jwt;

import com.masterlearning.platform.security.config.SecurityProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtSecretStrengthRegressionTest {
    @Test
    void refusesSigningSecretsShorterThanThirtyTwoUtf8Bytes() {
        var properties = new SecurityProperties(
                12,
                new SecurityProperties.Jwt("too-short-secret", 15, "trusted-issuer"),
                new SecurityProperties.Cors(new String[]{"http://localhost"})
        );

        assertThrows(IllegalStateException.class, () -> new JwtService(properties));
    }
}
