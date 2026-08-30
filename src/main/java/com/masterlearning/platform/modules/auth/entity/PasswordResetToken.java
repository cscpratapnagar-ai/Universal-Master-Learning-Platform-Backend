package com.masterlearning.platform.modules.auth.entity;

import com.masterlearning.platform.modules.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens", indexes = @Index(name = "idx_password_reset_token_hash", columnList = "token_hash", unique = true))
public class PasswordResetToken {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "token_hash", nullable = false, length = 64) private String tokenHash;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(nullable = false) private boolean used = false;
    protected PasswordResetToken() {}
    public PasswordResetToken(String tokenHash, User user, Instant expiresAt){this.tokenHash=tokenHash;this.user=user;this.expiresAt=expiresAt;}
    public boolean isUsable(){return !used && Instant.now().isBefore(expiresAt);}
    public User getUser(){return user;}
    public void markUsed(){used=true;}
}
