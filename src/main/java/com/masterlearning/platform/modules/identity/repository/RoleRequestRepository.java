package com.masterlearning.platform.modules.identity.repository;

import com.masterlearning.platform.modules.identity.entity.RoleRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Modifying
    @Query(value = """
        UPDATE role_requests
        SET status = 'REJECTED',
            reviewed_at = CURRENT_TIMESTAMP,
            reviewed_by = :reviewerId,
            rejection_reason = :reason
        WHERE id = :requestId
          AND status = 'PENDING'
        """, nativeQuery = true)
    int rejectPending(
            @Param("requestId") UUID requestId,
            @Param("reviewerId") UUID reviewerId,
            @Param("reason") String reason
    );
}
