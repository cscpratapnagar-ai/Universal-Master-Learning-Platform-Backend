package com.masterlearning.platform.modules.organization.security;

import com.masterlearning.platform.modules.organization.repository.OrganizationMemberRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("organizationAuthorization")
public class OrganizationAuthorizationService {

    private final OrganizationMemberRepository members;

    public OrganizationAuthorizationService(OrganizationMemberRepository members) {
        this.members = members;
    }

    public boolean isCurrentUserMember(UUID organizationId) {
        if (organizationId == null) {
            return false;
        }

        return members.findByOrganizationIdAndUserId(
                        organizationId,
                        SecurityUtils.getCurrentUserId()
                )
                .filter(member -> member.isActive() && member.getOrganization().isActive())
                .isPresent();
    }

    public boolean canAccessOrganization(UUID organizationId) {
        return isCurrentUserMember(organizationId);
    }
}
