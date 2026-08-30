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
import com.masterlearning.platform.modules.user.entity.User;
import com.masterlearning.platform.modules.user.mapper.UserMapper;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.security.config.SecurityProperties;
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

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final SecurityProperties securityProperties;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserMapper userMapper,
            SecurityProperties securityProperties
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.securityProperties = securityProperties;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists");
        }

        User user = new User(
                email,
                passwordEncoder.encode(request.password()),
                request.firstName().trim(),
                request.lastName() == null ? null : request.lastName().trim()
        );

        roleRepository.findByCode("LEARNER").ifPresent(user::assignRole);
        return issueTokens(userRepository.save(user));
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
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(hash(request.refreshToken()))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (!storedToken.isActive()) {
            throw new UnauthorizedException("Refresh token is expired or revoked");
        }

        storedToken.revoke();
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
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hash(request.token()))
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired password reset token"));
        if (!token.isUsable()) throw new UnauthorizedException("Invalid or expired password reset token");
        User user = token.getUser();
        user.updatePasswordHash(passwordEncoder.encode(request.newPassword()));
        token.markUsed();
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
