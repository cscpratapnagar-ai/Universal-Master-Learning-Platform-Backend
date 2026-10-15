package com.masterlearning.platform.security.jwt;

import com.masterlearning.platform.security.config.SecurityProperties;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtInvalidTokenRegressionTest {
    private static final String TRUSTED_KEY =
            "this-is-a-test-secret-key-that-is-long-enough-for-hmac-sha256";
    private static final String OTHER_KEY =
            "another-test-secret-key-that-is-long-enough-for-hmac-sha256";

    private JwtService service() {
        return new JwtService(new SecurityProperties(
                12,
                new SecurityProperties.Jwt(TRUSTED_KEY, 15, "trusted-issuer"),
                new SecurityProperties.Cors(new String[]{"http://localhost"})
        ));
    }

    @Test
    void rejectsTokenSignedWithDifferentKey() {
        String token = Jwts.builder()
                .issuer("trusted-issuer")
                .subject(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(OTHER_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThrows(JwtException.class, () -> service().extractUserId(token));
    }

    @Test
    void rejectsExpiredTokenSignedWithTrustedKey() {
        String token = Jwts.builder()
                .issuer("trusted-issuer")
                .subject(UUID.randomUUID().toString())
                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(TRUSTED_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThrows(JwtException.class, () -> service().extractUserId(token));
    }
}
