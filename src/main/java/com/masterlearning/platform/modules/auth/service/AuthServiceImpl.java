package com.masterlearning.platform.modules.auth.service;

import com.masterlearning.platform.common.exception.ConflictException;
import com.masterlearning.platform.common.exception.UnauthorizedException;
import com.masterlearning.platform.modules.auth.dto.request.*;
import com.masterlearning.platform.modules.auth.dto.request.LogoutRequest;
import com.masterlearning.platform.modules.auth.dto.request.RefreshTokenRequest;
import com.masterlearning.platform.modules.auth.dto.request.RegisterRequest;
import com.masterlearning.platform.modules.auth.dto.response.AuthResponse;
import com.masterlearning.platform.modules.auth.entity.RefreshToken;
import com.masterlearning.platform.modules.auth.repository.RefreshTokenRepository;
import com.masterlearning.platform.modules.auth.repository.PasswordResetTokenRepository;
import com.masterlearning.platform.modules.auth.entity.PasswordResetToken;
import com.masterlearning.platform.modules.identity.repository.RoleRepository;
import com.masterlearning.platform.modules.identity.entity.RoleRequest;
import com.masterlearning.platform.modules.identity.repository.RoleRequestRepository;
import com.masterlearning.platform.modules.user.entity.User;
import com.masterlearning.platform.modules.user.mapper.UserMapper;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.security.jwt.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {
    private static final java.util.Set<String> PUBLIC_SIGNUP_REQUESTABLE_ROLES = java.util.Set.of("TEACHER", "INSTRUCTOR", "ORG_ADMIN");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleRequestRepository roleRequestRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            RoleRequestRepository roleRequestRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.roleRequestRepository = roleRequestRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists");
        }

        String requestedRole = request.requestedRole() == null ? "" : request.requestedRole().trim().toUpperCase();
        if (!requestedRole.isBlank() && !requestedRole.equals("LEARNER") && !PUBLIC_SIGNUP_REQUESTABLE_ROLES.contains(requestedRole)) {
            throw new IllegalArgumentException("This role cannot be requested during public signup");
        }

        User user = new User(
                email,
                passwordEncoder.encode(request.password()),
                request.firstName().trim(),
                request.lastName() == null ? null : request.lastName().trim()
        );

        roleRepository.findByCode("LEARNER").ifPresent(user::assignRole);
        User saved = userRepository.save(user);
        if (!requestedRole.isBlank() && !requestedRole.equals("LEARNER")) {
            roleRequestRepository.save(new RoleRequest(saved, requestedRole, "Requested during signup"));
        }
        return issueTokens(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return issueTokens(user);
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        String tokenHash = hash(request.refreshToken());
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        Instant now = Instant.now();
        int revoked = refreshTokenRepository.revokeIfActive(tokenHash, now, now);
        if (revoked != 1) {
            throw new UnauthorizedException("Refresh token is expired or revoked");
        }

        if (!storedToken.getUser().isEnabled()) {
            throw new UnauthorizedException("Account is disabled");
        }

        return issueTokens(storedToken.getUser());
    }

    @Override
    public void logout(LogoutRequest request) {
        refreshTokenRepository.findByTokenHash(hash(request.refreshToken()))
                .ifPresent(RefreshToken::revoke);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase()).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUser_Id(user.getId());
            String rawToken = UUID.randomUUID() + "-" + UUID.randomUUID();
            passwordResetTokenRepository.save(new PasswordResetToken(hash(rawToken), user, Instant.now().plusSeconds(15 * 60)));
            // TODO: Email provider will deliver the raw token. Never persist or log it in production.
        });
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = hash(request.token());
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired password reset token"));

        int consumed = passwordResetTokenRepository.markUsedIfUsable(tokenHash, Instant.now());
        if (consumed != 1) {
            throw new UnauthorizedException("Invalid or expired password reset token");
        }

        User user = token.getUser();
        user.updatePasswordHash(passwordEncoder.encode(request.newPassword()));
        refreshTokenRepository.deleteByUser_Id(user.getId());
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = UUID.randomUUID() + "-" + UUID.randomUUID();

        RefreshToken storedToken = new RefreshToken(
                hash(refreshToken),
                user,
                Instant.now().plusSeconds(30L * 24L * 60L * 60L)
        );
        refreshTokenRepository.save(storedToken);

        return new AuthResponse(accessToken, refreshToken, "Bearer", userMapper.toResponse(user));
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(value.getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
