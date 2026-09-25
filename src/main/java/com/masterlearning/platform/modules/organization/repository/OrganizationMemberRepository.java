package com.masterlearning.platform.modules.organization.repository;

import com.masterlearning.platform.modules.organization.entity.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID> {

    boolean existsByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    @Query("select m from OrganizationMember m join fetch m.organization o join fetch m.user u where u.id = :userId order by m.active desc, o.name asc")
    List<OrganizationMember> findAllByUserId(@Param("userId") UUID userId);

    List<OrganizationMember> findAllByUserIdAndActiveTrue(UUID userId);

    @Query("select m from OrganizationMember m join fetch m.organization o join fetch m.user u where u.id = :userId and m.active = false")
    List<OrganizationMember> findAllByUserIdAndActiveFalse(@Param("userId") UUID userId);

    @Query("select m from OrganizationMember m join fetch m.organization o join fetch m.user u where o.id = :organizationId order by m.active desc, m.createdAt asc")
    List<OrganizationMember> findAllByOrganizationId(@Param("organizationId") UUID organizationId);

    @Query("select m from OrganizationMember m join fetch m.organization o where o.id = :organizationId and m.user.id = :userId")
    Optional<OrganizationMember> findByOrganizationIdAndUserId(@Param("organizationId") UUID organizationId, @Param("userId") UUID userId);

    long countByOrganizationId(UUID organizationId);

    long countByOrganizationIdAndActiveTrue(UUID organizationId);

    long countByOrganizationIdAndActiveFalse(UUID organizationId);
}
