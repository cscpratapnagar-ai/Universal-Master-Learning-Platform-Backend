package com.masterlearning.platform.core.security;
import jakarta.persistence.*; import java.time.Instant; import java.util.*;
@Entity @Table(name="refresh_tokens")
public class RefreshToken {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false) private User user;
 @Column(name="token_hash",nullable=false,unique=true,length=255) private String tokenHash;
 @Column(name="family_id",nullable=false) private UUID familyId;
 @Column(name="expires_at",nullable=false) private Instant expiresAt;
 @Column(name="revoked_at") private Instant revokedAt;
 @Column(name="replaced_by_token_id") private UUID replacedByTokenId;
 @Column(name="created_at",nullable=false) private Instant createdAt=Instant.now();
 @Column(name="last_used_at") private Instant lastUsedAt;
 @Column(name="user_agent",length=500) private String userAgent;
 @Column(name="ip_address",length=64) private String ipAddress;
 protected RefreshToken(){}
 public RefreshToken(User user,String hash,UUID family,Instant expiresAt,String ua,String ip){this.user=user;this.tokenHash=hash;this.familyId=family;this.expiresAt=expiresAt;this.userAgent=ua;this.ipAddress=ip;}
 public boolean isUsable(){return revokedAt==null && expiresAt.isAfter(Instant.now());}
 public void revoke(){revokedAt=Instant.now();} public void used(){lastUsedAt=Instant.now();}
 public UUID getId(){return id;} public User getUser(){return user;} public UUID getFamilyId(){return familyId;}
}