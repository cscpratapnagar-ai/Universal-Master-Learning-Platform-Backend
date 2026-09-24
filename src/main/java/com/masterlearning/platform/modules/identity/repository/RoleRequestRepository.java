package com.masterlearning.platform.modules.identity.repository;

import com.masterlearning.platform.modules.identity.entity.RoleRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;
import java.util.UUID;

public interface RoleRequestRepository extends JpaRepository<RoleRequest, UUID> {
    boolean existsByUser_IdAndRequestedRoleAndStatus(UUID userId, String requestedRole, String status);
    @EntityGraph(attributePaths = {"user", "reviewedBy"})
    List<RoleRequest> findByStatusOrderByCreatedAtAsc(String status);
    @EntityGraph(attributePaths = {"user", "reviewedBy"})
    List<RoleRequest> findByUser_IdOrderByCreatedAtDesc(UUID userId);
}
