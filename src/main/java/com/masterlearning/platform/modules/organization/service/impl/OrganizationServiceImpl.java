package com.masterlearning.platform.modules.organization.service.impl;

import com.masterlearning.platform.common.exception.*;
import com.masterlearning.platform.modules.course.entity.CourseStatus;
import com.masterlearning.platform.modules.course.repository.CourseRepository;
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

@Service
@Transactional
public class OrganizationServiceImpl implements OrganizationService {
 private final OrganizationRepository organizations;
 private final OrganizationMemberRepository members;
 private final UserRepository users;
 private final OrganizationMapper mapper;
 private final CourseRepository courses;

 public OrganizationServiceImpl(
         OrganizationRepository organizations,
         OrganizationMemberRepository members,
         UserRepository users,
         OrganizationMapper mapper,
         CourseRepository courses) {
   this.organizations=organizations;
   this.members=members;
   this.users=users;
   this.mapper=mapper;
   this.courses=courses;
 }

 public OrganizationResponse create(CreateOrganizationRequest r){
   String code=r.code().trim().toUpperCase();
   if(organizations.existsByCode(code))throw new ConflictException("Organization code already exists");
   return mapper.toResponse(organizations.save(new Organization(code,r.name().trim(),r.description())));
 }

 public OrganizationResponse update(UUID id,UpdateOrganizationRequest r){
   Organization o=find(id);
   o.update(r.name().trim(),r.description());
   return mapper.toResponse(o);
 }

 public OrganizationProfileResponse updateProfile(UUID id,UpdateOrganizationProfileRequest r){
   Organization o=find(id);
   String slug=r.slug()==null?null:r.slug().trim().toLowerCase();
   if(slug!=null&&!slug.equals(o.getSlug())&&organizations.existsBySlug(slug))
     throw new ConflictException("Organization slug already exists");
   o.updateProfile(slug,trim(r.legalName()),trim(r.displayName()),trim(r.organizationType()),
     trim(r.registrationNumber()),r.establishedDate(),trim(r.primaryEmail()),trim(r.primaryPhone()),
     trim(r.alternatePhone()),trim(r.website()),trim(r.addressLine()),trim(r.country()),trim(r.state()),
     trim(r.city()),trim(r.district()),trim(r.postalCode()),trim(r.logoUrl()),trim(r.coverImageUrl()),
     trim(r.primaryColor()),trim(r.secondaryColor()));
   return profile(o);
 }

 @Transactional(readOnly=true)
 public OrganizationProfileResponse getProfile(UUID id){return profile(find(id));}

 public OrganizationProfileResponse updateStatus(UUID id,UpdateOrganizationStatusRequest r){
   Organization o=find(id);
   o.changeStatus(r.status());
   return profile(o);
 }

 @Transactional(readOnly=true)
 public OrganizationResponse getById(UUID id){return mapper.toResponse(find(id));}

 @Transactional(readOnly=true)
 public List<OrganizationResponse> getAll(){return organizations.findAll().stream().map(mapper::toResponse).toList();}

 public void deactivate(UUID id){find(id).deactivate();}

 public void addMember(UUID organizationId,AddOrganizationMemberRequest r){
   Organization o=find(organizationId);
   if(!o.isActive())throw new ConflictException("Organization is inactive");
   if(members.existsByOrganizationIdAndUserId(organizationId,r.userId()))
     throw new ConflictException("User is already an organization member");
   User u=users.findById(r.userId()).orElseThrow(()->new ResourceNotFoundException("User not found"));
   members.save(new OrganizationMember(o,u));
 }

 public void inviteMember(UUID organizationId,InviteOrganizationMemberRequest r){
   Organization o=find(organizationId);
   if(!o.isActive())throw new ConflictException("Organization is inactive");
   User u=users.findByEmailIgnoreCase(r.email().trim())
     .orElseThrow(()->new ResourceNotFoundException("No user exists with that email"));
   addMember(organizationId,new AddOrganizationMemberRequest(u.getId()));
 }

 public void deactivateMember(UUID organizationId,UUID memberId){
   find(organizationId);
   OrganizationMember member=members.findById(memberId)
     .orElseThrow(()->new ResourceNotFoundException("Organization member not found"));
   if(!organizationId.equals(member.getOrganization().getId()))
     throw new ResourceNotFoundException("Organization member not found");
   member.deactivate();
 }

 @Transactional(readOnly=true)
 public List<OrganizationMemberResponse> getMembers(UUID organizationId){
   find(organizationId);
   return members.findAllByOrganizationIdAndActiveTrue(organizationId).stream()
     .map(m->new OrganizationMemberResponse(m.getId(),m.getUser().getId(),m.getUser().getEmail(),
       m.getUser().getFirstName(),m.getUser().getLastName(),m.isActive()))
     .toList();
 }

 @Transactional(readOnly=true)
 public OrganizationOverviewResponse getOverview(UUID organizationId){
   Organization o=find(organizationId);
   long totalMembers=members.countByOrganizationId(organizationId);
   long activeMembers=members.countByOrganizationIdAndActiveTrue(organizationId);
   long inactiveMembers=members.countByOrganizationIdAndActiveFalse(organizationId);
   long totalCourses=courses.countByOrganizationId(organizationId);
   long publishedCourses=courses.countByOrganizationIdAndStatus(organizationId,CourseStatus.PUBLISHED);
   long draftCourses=courses.countByOrganizationIdAndStatus(organizationId,CourseStatus.DRAFT);
   long archivedCourses=courses.countByOrganizationIdAndStatus(organizationId,CourseStatus.ARCHIVED);
   return new OrganizationOverviewResponse(
     o.getId(),o.getName(),o.getCode(),o.getStatus(),o.isActive(),
     totalMembers,activeMembers,inactiveMembers,totalCourses,publishedCourses,draftCourses,archivedCourses
   );
 }

 @Transactional
 public List<OrganizationResponse> getCurrentUserOrganizations(){
  UUID userId=SecurityUtils.getCurrentUserId();
  var existing=members.findAllByUserIdAndActiveTrue(userId);
  if(existing.isEmpty()){
    User user=users.findById(userId).orElseThrow(()->new ResourceNotFoundException("User not found"));
    boolean orgAdmin=user.getRoles().stream().anyMatch(role -> "ORG_ADMIN".equals(role.getCode()));
    if(orgAdmin){
      String suffix=user.getId().toString().replace("-","").substring(0,10).toUpperCase(Locale.ROOT);
      String code="ORG-"+suffix;
      Organization organization=organizations.findByCode(code).orElseGet(() -> organizations.save(
        new Organization(code,buildOrganizationName(user),"Organization workspace created during organization administrator onboarding.")
      ));
      if(!members.existsByOrganizationIdAndUserId(organization.getId(),userId)){
        members.save(new OrganizationMember(organization,user));
      }
      existing=members.findAllByUserIdAndActiveTrue(userId);
    }
  }
  return existing.stream().map(m->mapper.toResponse(m.getOrganization())).toList();
 }

 private String buildOrganizationName(User user){
   String first=user.getFirstName()==null?"":user.getFirstName().trim();
   String last=user.getLastName()==null?"":user.getLastName().trim();
   String name=(first+" "+last).trim();
   return name.isBlank()?"Organization Workspace":name+" Organization";
 }

 private Organization find(UUID id){
   return organizations.findById(id).orElseThrow(()->new ResourceNotFoundException("Organization not found"));
 }

 private String trim(String value){return value==null?null:value.trim();}

 private OrganizationProfileResponse profile(Organization o){
   return new OrganizationProfileResponse(o.getId(),o.getCode(),o.getName(),o.getDescription(),o.isActive(),
     o.getSlug(),o.getLegalName(),o.getDisplayName(),o.getOrganizationType(),o.getRegistrationNumber(),
     o.getEstablishedDate(),o.getPrimaryEmail(),o.getPrimaryPhone(),o.getAlternatePhone(),o.getWebsite(),
     o.getAddressLine(),o.getCountry(),o.getState(),o.getCity(),o.getDistrict(),o.getPostalCode(),
     o.getLogoUrl(),o.getCoverImageUrl(),o.getPrimaryColor(),o.getSecondaryColor(),o.getStatus());
 }
}
