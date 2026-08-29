package com.masterlearning.platform.core.security;
import io.jsonwebtoken.*; import io.jsonwebtoken.security.Keys; import org.springframework.stereotype.Service;
import javax.crypto.SecretKey; import java.nio.charset.StandardCharsets; import java.time.*; import java.util.*;
@Service
public class JwtService {
 private final SecurityProperties p; private final SecretKey key;
 public JwtService(SecurityProperties p){this.p=p;this.key=Keys.hmacShaKeyFor(p.jwt().secret().getBytes(StandardCharsets.UTF_8));}
 public String accessToken(User u){Instant now=Instant.now(); return Jwts.builder().subject(u.getId().toString()).claim("email",u.getEmail()).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(p.jwt().accessTokenMinutes()*60L))).signWith(key).compact();}
 public UUID subject(String token){return UUID.fromString(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject());}
}