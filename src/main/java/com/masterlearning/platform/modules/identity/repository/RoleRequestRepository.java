package com.masterlearning.platform.modules.identity.repository;

import com.masterlearning.platform.modules.identity.entity.RoleRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRequestRepository extends JpaRepository<RoleRequest, UUID> {
    boolean existsByUser_IdAndRequestedRoleAndStatus(UUID userId, String requestedRole, String status);

    @EntityGraph(attributePaths = {"user", "user.roles", "reviewedBy", "reviewedBy.roles"})
    List<RoleRequest> findByStatusOrderByCreatedAtAsc(String status);

    @EntityGraph(attributePaths = {"user", "user.roles", "reviewedBy", "reviewedBy.roles"})
    Optional<RoleRequest> findById(UUID id);

    @EntityGraph(attributePaths = {"user", "reviewedBy"})
    List<RoleRequest> findByUser_IdOrderByCreatedAtDesc(UUID userId);
}
