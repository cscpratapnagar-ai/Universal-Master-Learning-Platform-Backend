package com.masterlearning.platform.modules.organization.service.impl;

import com.masterlearning.platform.common.exception.*;
import com.masterlearning.platform.modules.organization.dto.request.*;
import com.masterlearning.platform.modules.organization.dto.response.*;
import com.masterlearning.platform.modules.organization.entity.*;
import com.masterlearning.platform.modules.organization.mapper.OrganizationMapper;
import com.masterlearning.platform.modules.organization.repository.*;
import com.masterlearning.platform.modules.organization.service.OrganizationService;
import com.masterlearning.platform.modules.user.entity.User;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @Transactional
public class OrganizationServiceImpl implements OrganizationService {
 private final OrganizationRepository organizations; private final OrganizationMemberRepository members; private final UserRepository users; private final OrganizationMapper mapper;
 public OrganizationServiceImpl(OrganizationRepository organizations,OrganizationMemberRepository members,UserRepository users,OrganizationMapper mapper){this.organizations=organizations;this.members=members;this.users=users;this.mapper=mapper;}
 public OrganizationResponse create(CreateOrganizationRequest r){String code=r.code().trim().toUpperCase();if(organizations.existsByCode(code))throw new ConflictException("Organization code already exists");return mapper.toResponse(organizations.save(new Organization(code,r.name().trim(),r.description())));}
 public OrganizationResponse update(UUID id,UpdateOrganizationRequest r){Organization o=find(id);o.update(r.name().trim(),r.description());return mapper.toResponse(o);}
 @Transactional(readOnly=true) public OrganizationResponse getById(UUID id){return mapper.toResponse(find(id));}
 @Transactional(readOnly=true) public List<OrganizationResponse> getAll(){return organizations.findAll().stream().map(mapper::toResponse).toList();}
 public void deactivate(UUID id){find(id).deactivate();}
 public void addMember(UUID organizationId,AddOrganizationMemberRequest r){Organization o=find(organizationId);if(members.existsByOrganizationIdAndUserId(organizationId,r.userId()))throw new ConflictException("User is already an organization member");User u=users.findById(r.userId()).orElseThrow(()->new ResourceNotFoundException("User not found"));members.save(new OrganizationMember(o,u));}
 @Transactional(readOnly=true) public List<OrganizationMemberResponse> getMembers(UUID organizationId){find(organizationId);return members.findAllByOrganizationIdAndActiveTrue(organizationId).stream().map(m->new OrganizationMemberResponse(m.getId(),m.getUser().getId(),m.getUser().getEmail(),m.getUser().getFirstName(),m.getUser().getLastName(),m.isActive())).toList();}
 @Transactional(readOnly=true) public List<OrganizationResponse> getCurrentUserOrganizations(){return members.findAllByUserIdAndActiveTrue(SecurityUtils.getCurrentUserId()).stream().map(m->mapper.toResponse(m.getOrganization())).toList();}
 private Organization find(UUID id){return organizations.findById(id).orElseThrow(()->new ResourceNotFoundException("Organization not found"));}
}