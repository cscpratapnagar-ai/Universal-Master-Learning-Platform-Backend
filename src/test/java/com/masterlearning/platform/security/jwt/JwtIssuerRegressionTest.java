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

class JwtIssuerRegressionTest {
    @Test
    void rejectsTokenWithUnexpectedIssuer() {
        String key = "this-is-a-test-secret-key-that-is-long-enough-for-hmac-sha256";
        var properties = new SecurityProperties(12,
                new SecurityProperties.Jwt(key, 15, "trusted-issuer"),
                new SecurityProperties.Cors(new String[]{"http://localhost"}));
        var service = new JwtService(properties);
        String token = Jwts.builder()
                .issuer("unexpected-issuer")
                .subject(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8)))
                .compact();
        assertThrows(JwtException.class, () -> service.extractUserId(token));
    }
}
