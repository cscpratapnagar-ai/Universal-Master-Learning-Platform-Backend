package com.masterlearning.platform.audit;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="audit_logs")
public class AuditLog {
    @Id private UUID id;
    @Column(name="actor_user_id") private UUID actorUserId;
    @Column(nullable=false,length=100) private String action;
    @Column(name="resource_type",nullable=false,length=80) private String resourceType;
    @Column(name="resource_id",length=120) private String resourceId;
    @Column(nullable=false,length=30) private String outcome;
    @Column(length=2000) private String details;
    @Column(name="ip_address",length=64) private String ipAddress;
    @Column(name="created_at",nullable=false) private LocalDateTime createdAt;
    protected AuditLog(){}
    public AuditLog(UUID actorUserId,String action,String resourceType,String resourceId,String outcome,String details,String ipAddress){
        this.id=UUID.randomUUID();this.actorUserId=actorUserId;this.action=action;this.resourceType=resourceType;this.resourceId=resourceId;
        this.outcome=outcome;this.details=details;this.ipAddress=ipAddress;this.createdAt=LocalDateTime.now();
    }
    public UUID getId(){return id;} public UUID getActorUserId(){return actorUserId;} public String getAction(){return action;}
    public String getResourceType(){return resourceType;} public String getResourceId(){return resourceId;} public String getOutcome(){return outcome;}
    public String getDetails(){return details;} public String getIpAddress(){return ipAddress;} public LocalDateTime getCreatedAt(){return createdAt;}
}