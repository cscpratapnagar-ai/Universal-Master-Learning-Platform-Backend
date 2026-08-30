package com.masterlearning.platform.modules.organization.repository;

import com.masterlearning.platform.modules.organization.entity.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID> {

    boolean existsByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    List<OrganizationMember> findAllByUserIdAndActiveTrue(UUID userId);

    List<OrganizationMember> findAllByOrganizationIdAndActiveTrue(UUID organizationId);

    Optional<OrganizationMember> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);
}
