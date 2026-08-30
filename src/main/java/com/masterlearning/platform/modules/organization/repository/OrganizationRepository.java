package com.masterlearning.platform.modules.organization.repository;

import com.masterlearning.platform.modules.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    Optional<Organization> findByCode(String code);
    boolean existsByCode(String code);
    Optional<Organization> findBySlug(String slug);
    boolean existsBySlug(String slug);
}