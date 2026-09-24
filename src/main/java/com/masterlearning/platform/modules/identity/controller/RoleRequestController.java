package com.masterlearning.platform.modules.identity.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.identity.dto.RoleRequestCreate;
import com.masterlearning.platform.modules.identity.entity.RoleRequest;
import com.masterlearning.platform.modules.identity.repository.RoleRepository;
import com.masterlearning.platform.modules.identity.repository.RoleRequestRepository;
import com.masterlearning.platform.modules.user.entity.User;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.modules.user.dto.request.UpdateUserRolesRequest;
import com.masterlearning.platform.modules.user.service.UserManagementService;
import com.masterlearning.platform.modules.organization.entity.Organization;
import com.masterlearning.platform.modules.organization.entity.OrganizationMember;
import com.masterlearning.platform.modules.organization.repository.OrganizationRepository;
import com.masterlearning.platform.modules.organization.repository.OrganizationMemberRepository;
import com.masterlearning.platform.security.authority.CurrentUserPrincipal;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class RoleRequestController {
    private static final Set<String> REQUESTABLE = Set.of("TEACHER","INSTRUCTOR","ORG_ADMIN");
    private final RoleRequestRepository requests;
    private final UserRepository users;
    private final RoleRepository roles;
    private final UserManagementService userManagement;
    private final OrganizationRepository organizations;
    private final OrganizationMemberRepository organizationMembers;
    private static final Set<String> SUPER_ADMIN_ONLY_ROLES = Set.of("ORG_ADMIN");

    public RoleRequestController(RoleRequestRepository requests, UserRepository users, RoleRepository roles, UserManagementService userManagement, OrganizationRepository organizations, OrganizationMemberRepository organizationMembers) {
        this.requests=requests; this.users=users; this.roles=roles; this.userManagement=userManagement; this.organizations=organizations; this.organizationMembers=organizationMembers;
    }

    @PostMapping("/role-requests")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String,Object>> create(@AuthenticationPrincipal CurrentUserPrincipal principal,
                                                    @Valid @RequestBody RoleRequestCreate request) {
        User user=users.findById(principal.userId()).orElseThrow(()->new EntityNotFoundException("User not found"));
        String code=request.requestedRole().trim().toUpperCase();
        if(!REQUESTABLE.contains(code)) throw new IllegalArgumentException("This role cannot be requested");
        if(user.getRoles().stream().anyMatch(r->code.equals(r.getCode()))) throw new IllegalArgumentException("You already have this role");
        if(requests.existsByUser_IdAndRequestedRoleAndStatus(user.getId(),code,"PENDING")) throw new IllegalArgumentException("A request for this role is already pending");
        RoleRequest saved=requests.save(new RoleRequest(user,code,request.reason()));
        return ApiResponse.success("Role request submitted", Map.of("id",saved.getId(),"requestedRole",code,"status","PENDING"));
    }

    @GetMapping("/role-requests/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Map<String,Object>>> mine(@AuthenticationPrincipal CurrentUserPrincipal principal) {
        return ApiResponse.success("Role requests retrieved", requests.findByUser_IdOrderByCreatedAtDesc(principal.userId()).stream().map(this::view).toList());
    }

    @GetMapping("/admin/role-requests")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<List<Map<String,Object>>> pending() {
        return ApiResponse.success("Pending role requests retrieved", requests.findByStatusOrderByCreatedAtAsc("PENDING").stream().map(this::view).toList());
    }

    @PostMapping("/admin/role-requests/{id}/approve")
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<Map<String,Object>> approve(@PathVariable UUID id, @AuthenticationPrincipal CurrentUserPrincipal principal) {
        RoleRequest request=requests.findById(id).orElseThrow(()->new EntityNotFoundException("Role request not found"));
        if(!"PENDING".equals(request.getStatus())) throw new IllegalArgumentException("Only pending requests can be approved");
        User reviewer=users.findWithAuthoritiesById(principal.userId()).orElseThrow(()->new EntityNotFoundException("Reviewer not found"));
        if (SUPER_ADMIN_ONLY_ROLES.contains(request.getRequestedRole()) && reviewer.getRoles().stream().noneMatch(role -> "SUPER_ADMIN".equals(role.getCode()))) {
            throw new org.springframework.security.access.AccessDeniedException("Only a super administrator can approve organization administrator requests");
        }
        roles.findByCode(request.getRequestedRole()).orElseThrow(()->new EntityNotFoundException("Requested role does not exist: " + request.getRequestedRole()));
        Set<String> roleCodes = new HashSet<>(request.getUser().getRoles().stream().map(role -> role.getCode()).toList());
        roleCodes.add(request.getRequestedRole());
        userManagement.updateRoles(request.getUser().getId(), new UpdateUserRolesRequest(roleCodes));

        UUID organizationId = null;
        if ("ORG_ADMIN".equals(request.getRequestedRole())) {
            User applicant = request.getUser();
            var organization = organizations.findAll().stream()
                    .filter(o -> applicant.getEmail().equalsIgnoreCase(o.getPrimaryEmail()))
                    .findFirst()
                    .orElseGet(() -> {
                        String suffix = applicant.getId().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
                        String code = "ORG-" + suffix;
                        Organization created = organizations.save(new Organization(
                                code,
                                buildOrganizationName(applicant),
                                "Organization workspace created during approved organization administrator onboarding."
                        ));
                        created.updateProfile(
                                "org-" + suffix.toLowerCase(Locale.ROOT),
                                created.getName(),
                                created.getName(),
                                "INSTITUTE",
                                null,
                                null,
                                applicant.getEmail(),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        );
                        return created;
                    });
            if (!organizationMembers.existsByOrganizationIdAndUserId(organization.getId(), applicant.getId())) {
                organizationMembers.save(new OrganizationMember(organization, applicant));
            }
            organizationId = organization.getId();
        }

        request.approve(reviewer);
        requests.save(request);
        Map<String,Object> response = new LinkedHashMap<>(view(request));
        if (organizationId != null) response.put("organizationId", organizationId);
        return ApiResponse.success("Role request approved", response);
    }

    @PostMapping("/admin/role-requests/{id}/reject")
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<Map<String,Object>> reject(@PathVariable UUID id, @RequestParam(required=false) String reason,
                                                    @AuthenticationPrincipal CurrentUserPrincipal principal) {
        RoleRequest request=requests.findById(id).orElseThrow(()->new EntityNotFoundException("Role request not found"));
        if(!"PENDING".equals(request.getStatus())) throw new IllegalArgumentException("Only pending requests can be rejected");
        User reviewer=users.findWithAuthoritiesById(principal.userId()).orElseThrow(()->new EntityNotFoundException("Reviewer not found"));
        String rejectionReason=reason == null || reason.isBlank() ? "Request rejected by administrator" : reason.trim();
        int updated=requests.rejectPending(request.getId(), reviewer.getId(), rejectionReason);
        if (updated != 1) throw new IllegalArgumentException("Role request was already processed");
        RoleRequest rejected=requests.findById(request.getId()).orElseThrow(()->new EntityNotFoundException("Rejected role request could not be reloaded"));
        return ApiResponse.success("Role request rejected", view(rejected));
    }

    private String buildOrganizationName(User user) {
        String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String last = user.getLastName() == null ? "" : user.getLastName().trim();
        String name = (first + " " + last).trim();
        return name.isBlank() ? "Organization Workspace" : name + " Organization";
    }

    private Map<String,Object> view(RoleRequest r) {
        return Map.of("id",r.getId(),"userId",r.getUser().getId(),"userName",(r.getUser().getFirstName()+" "+(r.getUser().getLastName()==null?"":r.getUser().getLastName())).trim(),"userEmail",r.getUser().getEmail(),"requestedRole",r.getRequestedRole(),"status",r.getStatus(),"reason",r.getReason()==null?"":r.getReason(),"rejectionReason",r.getRejectionReason()==null?"":r.getRejectionReason());
    }
}
