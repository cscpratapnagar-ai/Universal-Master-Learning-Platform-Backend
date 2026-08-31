package com.masterlearning.platform.modules.user.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import com.masterlearning.platform.modules.identity.entity.Role;
import jakarta.persistence.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
public class User extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 255) private String email;
    @Column(name = "password_hash", nullable = false, length = 255) private String passwordHash;
    @Column(name = "first_name", nullable = false, length = 100) private String firstName;
    @Column(name = "last_name", length = 100) private String lastName;
    @Column(nullable = false) private boolean enabled = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    protected User() {}

    public User(String email, String passwordHash, String firstName, String lastName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public boolean isEnabled() { return enabled; }
    public Set<Role> getRoles() { return Set.copyOf(roles); }

    public void assignRole(Role role) { roles.add(role); }
    public void replaceRoles(Collection<Role> newRoles) {
        roles.clear();
        roles.addAll(newRoles);
    }
    public void updatePasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void enable() { this.enabled = true; }
    public void disable() { this.enabled = false; }
}