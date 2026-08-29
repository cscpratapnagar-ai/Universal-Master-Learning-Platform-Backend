package com.masterlearning.platform.core.security;
import org.springframework.data.jpa.repository.*; import java.util.*;
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,UUID>{ Optional<RefreshToken> findByTokenHash(String tokenHash); List<RefreshToken> findByFamilyId(UUID familyId); }