package com.masterlearning.platform.modules.auth.repository;

import com.masterlearning.platform.modules.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            update PasswordResetToken token
               set token.used = true
             where token.tokenHash = :tokenHash
               and token.used = false
               and token.expiresAt > :now
            """)
    int markUsedIfUsable(
            @Param("tokenHash") String tokenHash,
            @Param("now") Instant now
    );

    void deleteByUser_Id(UUID userId);
}
