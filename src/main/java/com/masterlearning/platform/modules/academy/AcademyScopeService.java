package com.masterlearning.platform.modules.academy;

import com.masterlearning.platform.modules.organization.entity.OrganizationMember;
import com.masterlearning.platform.modules.organization.repository.OrganizationMemberRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AcademyScopeService {
    private final OrganizationMemberRepository members;

    public AcademyScopeService(OrganizationMemberRepository members) {
        this.members = members;
    }

    public boolean isSuperAdmin() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
    }

    public List<UUID> accessibleOrganizationIds() {
        if (isSuperAdmin()) return List.of();
        return members.findAllByUserIdAndActiveTrue(SecurityUtils.getCurrentUserId()).stream()
                .filter(m -> m.getOrganization().isActive())
                .map(OrganizationMember::getOrganization)
                .map(o -> o.getId())
                .distinct()
                .toList();
    }

    public boolean isGlobal() { return isSuperAdmin(); }
}