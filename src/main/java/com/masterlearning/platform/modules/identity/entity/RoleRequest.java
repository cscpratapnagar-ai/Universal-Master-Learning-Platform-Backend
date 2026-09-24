package com.masterlearning.platform.modules.identity.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import com.masterlearning.platform.modules.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "role_requests", indexes = {
        @Index(name = "idx_role_requests_user", columnList = "user_id"),
        @Index(name = "idx_role_requests_status", columnList = "status")
})
public class RoleRequest extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "requested_role", nullable = false, length = 50)
    private String requestedRole;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(length = 1000)
    private String reason;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    protected RoleRequest() {}

    public RoleRequest(User user, String requestedRole, String reason) {
        this.user = user;
        this.requestedRole = requestedRole;
        this.reason = reason;
    }

    public UUID getId(){ return id; }
    public User getUser(){ return user; }
    public String getRequestedRole(){ return requestedRole; }
    public String getStatus(){ return status; }
    public String getReason(){ return reason; }
    public Instant getReviewedAt(){ return reviewedAt; }
    public User getReviewedBy(){ return reviewedBy; }
    public String getRejectionReason(){ return rejectionReason; }

    public void approve(User reviewer){ status="APPROVED"; reviewedAt=Instant.now(); reviewedBy=reviewer; rejectionReason=null; }
    public void reject(User reviewer,String reason){ status="REJECTED"; reviewedAt=Instant.now(); reviewedBy=reviewer; rejectionReason=reason; }
}
