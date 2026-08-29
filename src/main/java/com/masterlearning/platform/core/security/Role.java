package com.masterlearning.platform.core.security;
import jakarta.persistence.*; import java.util.*;
@Entity @Table(name="roles")
public class Role {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(nullable=false,unique=true,length=80) private String code;
 @Column(nullable=false,length=120) private String name;
 @Column(length=500) private String description;
 @Column(name="system_role",nullable=false) private boolean systemRole;
 @ManyToMany(fetch=FetchType.LAZY) @JoinTable(name="role_permissions",joinColumns=@JoinColumn(name="role_id"),inverseJoinColumns=@JoinColumn(name="permission_id")) private Set<Permission> permissions=new HashSet<>();
 protected Role(){}
 public String getCode(){return code;} public Set<Permission> getPermissions(){return Collections.unmodifiableSet(permissions);}
}