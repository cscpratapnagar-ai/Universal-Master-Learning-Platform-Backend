package com.masterlearning.platform.modules.auth.service.impl;

import com.masterlearning.platform.common.exception.BadRequestException;
import com.masterlearning.platform.common.exception.ConflictException;
import com.masterlearning.platform.modules.auth.dto.request.*;
import com.masterlearning.platform.modules.auth.dto.response.AuthResponse;
import com.masterlearning.platform.modules.auth.entity.RefreshToken;
import com.masterlearning.platform.modules.auth.repository.RefreshTokenRepository;
import com.masterlearning.platform.modules.auth.service.AuthService;
import com.masterlearning.platform.modules.identity.entity.Role;
import com.masterlearning.platform.modules.identity.repository.RoleRepository;
import com.masterlearning.platform.modules.user.entity.User;
import com.masterlearning.platform.modules.user.mapper.UserMapper;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service @Transactional
public class AuthServiceImpl implements AuthService {
    private final UserRepository users; private final RoleRepository roles; private final RefreshTokenRepository tokens;
    private final PasswordEncoder encoder; private final JwtService jwt; private final UserMapper mapper; private final int refreshDays;
    public AuthServiceImpl(UserRepository users,RoleRepository roles,RefreshTokenRepository tokens,PasswordEncoder encoder,JwtService jwt,UserMapper mapper,@Value("${app.security.refresh-token-days:30}") int refreshDays){this.users=users;this.roles=roles;this.tokens=tokens;this.encoder=encoder;this.jwt=jwt;this.mapper=mapper;this.refreshDays=refreshDays;}
    public AuthResponse register(RegisterRequest r){String email=r.email().trim().toLowerCase();if(users.existsByEmailIgnoreCase(email))throw new ConflictException("Email is already registered");Role student=roles.findByCode("STUDENT").orElseThrow(()->new IllegalStateException("Default STUDENT role is missing"));User u=new User(email,encoder.encode(r.password()),r.firstName().trim(),r.lastName()==null||r.lastName().isBlank()?null:r.lastName().trim());u.assignRole(student);return issue(users.save(u));}
    @Transactional(readOnly=true) public AuthResponse login(LoginRequest r){User u=users.findByEmailIgnoreCase(r.email().trim()).orElseThrow(()->new BadRequestException("Invalid email or password"));if(!u.isEnabled()||!encoder.matches(r.password(),u.getPasswordHash()))throw new BadRequestException("Invalid email or password");return issue(u);}
    public AuthResponse refresh(RefreshTokenRequest r){RefreshToken t=tokens.findByTokenHash(hash(r.refreshToken())).orElseThrow(()->new BadRequestException("Invalid refresh token"));if(!t.isActive()||!t.getUser().isEnabled())throw new BadRequestException("Invalid refresh token");t.revoke();return issue(t.getUser());}
    public void logout(LogoutRequest r){tokens.findByTokenHash(hash(r.refreshToken())).ifPresent(RefreshToken::revoke);}
    private AuthResponse issue(User u){String access=jwt.generateAccessToken(u.getId(),u.getEmail());String raw=UUID.randomUUID()+"."+UUID.randomUUID();tokens.save(new RefreshToken(hash(raw),u,Instant.now().plus(refreshDays,ChronoUnit.DAYS)));return new AuthResponse(access,raw,"Bearer",mapper.toResponse(u));}
    private String hash(String v){try{return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(v.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException("Unable to hash token",e);}}
}