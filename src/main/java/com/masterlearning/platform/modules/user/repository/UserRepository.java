package com.masterlearning.platform.modules.user.repository;

import com.masterlearning.platform.modules.user.entity.User;
import com.masterlearning.platform.modules.organization.entity.OrganizationMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    @Query("""
            select distinct u
            from User u
            left join fetch u.roles
            """)
    List<User> findAllWithRoles();

    @Query("""
            select count(distinct u)
            from User u
            join u.roles r
            where u.enabled = true
              and upper(r.code) = 'SUPER_ADMIN'
            """)
    long countActiveSuperAdmins();

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findWithAuthoritiesById(UUID id);

    boolean existsByEmailIgnoreCase(String email);

    long countByEnabledTrue();

    @Query("select count(distinct u) from User u join u.roles r join OrganizationMember m on m.user.id = u.id where m.organization.id = :organizationId and m.active = true and upper(r.code) = upper(:roleCode)")
    long countActiveByOrganizationIdAndRoleCode(@org.springframework.data.repository.query.Param("organizationId") UUID organizationId, @org.springframework.data.repository.query.Param("roleCode") String roleCode);

    @Query("""
            select count(distinct u)
            from User u join u.roles r
            where upper(r.code) = upper(:roleCode)
            """)
    long countByRoleCode(@org.springframework.data.repository.query.Param("roleCode") String roleCode);

    @Query("""
            select count(distinct u)
            from User u join u.roles r
            where u.enabled = true and upper(r.code) = upper(:roleCode)
            """)
    long countEnabledByRoleCode(@org.springframework.data.repository.query.Param("roleCode") String roleCode);

    long countByCreatedAtAfter(Instant createdAt);

    @Query("""
            select r.code, count(distinct u)
            from User u
            join u.roles r
            group by r.code
            order by r.code
            """)
    List<Object[]> countUsersByRole();
}
