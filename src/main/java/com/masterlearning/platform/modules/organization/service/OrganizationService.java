package com.masterlearning.platform.modules.organization.service;
import com.masterlearning.platform.modules.organization.dto.request.*;
import com.masterlearning.platform.modules.organization.dto.response.*;
import java.util.*;
public interface OrganizationService {
 OrganizationResponse create(CreateOrganizationRequest request);
 OrganizationResponse update(UUID id,UpdateOrganizationRequest request);
 OrganizationResponse getById(UUID id);
 List<OrganizationResponse> getAll();
 void deactivate(UUID id);
 void addMember(UUID organizationId,AddOrganizationMemberRequest request);
 List<OrganizationMemberResponse> getMembers(UUID organizationId);
 List<OrganizationResponse> getCurrentUserOrganizations();
}