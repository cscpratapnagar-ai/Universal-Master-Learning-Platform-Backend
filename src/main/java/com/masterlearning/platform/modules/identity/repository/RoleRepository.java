package com.masterlearning.platform.modules.identity.repository;

import com.masterlearning.platform.modules.identity.entity.Role;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Role r where upper(r.code) = upper(:code)")
    Optional<Role> findByCodeForUpdate(@Param("code") String code);

    boolean existsByCode(String code);
}