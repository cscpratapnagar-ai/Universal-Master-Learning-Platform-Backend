package com.masterlearning.platform.core.security;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "users")
public class User {
 @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
 @Column(nullable=false,unique=true,length=320) private String email;
 @Column(name="password_hash",nullable=false,length=255) private String passwordHash;
 @Column(name="first_name",nullable=false,length=100) private String firstName;
 @Column(name="last_name",length=100) private String lastName;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private UserStatus status;
 @Column(name="email_verified",nullable=false) private boolean emailVerified;
 @Column(name="failed_login_attempts",nullable=false) private int failedLoginAttempts;
 @Column(name="locked_until") private Instant lockedUntil;
 @Column(name="last_login_at") private Instant lastLoginAt;
 @Column(name="password_changed_at") private Instant passwordChangedAt;
 @ManyToMany(fetch=FetchType.LAZY)
 @JoinTable(name="user_roles",joinColumns=@JoinColumn(name="user_id"),inverseJoinColumns=@JoinColumn(name="role_id"))
 private Set<Role> roles=new HashSet<>();
 protected User(){}
 public User(String email,String passwordHash,String firstName,String lastName){this.email=email;this.passwordHash=passwordHash;this.firstName=firstName;this.lastName=lastName;this.status=UserStatus.PENDING_VERIFICATION;}
 public UUID getId(){return id;} public String getEmail(){return email;} public String getPasswordHash(){return passwordHash;} public UserStatus getStatus(){return status;} public Set<Role> getRoles(){return Collections.unmodifiableSet(roles);}
 public boolean isLocked(){return status==UserStatus.LOCKED || (lockedUntil!=null && lockedUntil.isAfter(Instant.now()));}
 public void addRole(Role role){roles.add(role);} public void activate(){status=UserStatus.ACTIVE;} public void recordLogin(){failedLoginAttempts=0;lockedUntil=null;lastLoginAt=Instant.now();}
 public void recordFailedLogin(int maxAttempts,int lockMinutes){failedLoginAttempts++; if(failedLoginAttempts>=maxAttempts){lockedUntil=Instant.now().plusSeconds(lockMinutes*60L);status=UserStatus.LOCKED;}}
}