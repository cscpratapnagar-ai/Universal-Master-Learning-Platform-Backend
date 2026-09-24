package com.masterlearning.platform.modules.identity.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.identity.dto.RoleRequestCreate;
import com.masterlearning.platform.modules.identity.entity.RoleRequest;
import com.masterlearning.platform.modules.identity.repository.RoleRepository;
import com.masterlearning.platform.modules.identity.repository.RoleRequestRepository;
import com.masterlearning.platform.modules.user.entity.User;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.security.authority.CurrentUserPrincipal;
import jakarta.persistence.EntityNotFoundException;
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

    public RoleRequestController(RoleRequestRepository requests, UserRepository users, RoleRepository roles) {
        this.requests=requests; this.users=users; this.roles=roles;
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<Map<String,Object>> approve(@PathVariable UUID id, @AuthenticationPrincipal CurrentUserPrincipal principal) {
        RoleRequest request=requests.findById(id).orElseThrow(()->new EntityNotFoundException("Role request not found"));
        if(!"PENDING".equals(request.getStatus())) throw new IllegalArgumentException("Only pending requests can be approved");
        User reviewer=users.findById(principal.userId()).orElseThrow(()->new EntityNotFoundException("Reviewer not found"));
        roles.findByCode(request.getRequestedRole()).ifPresentOrElse(request.getUser()::assignRole,()->{throw new EntityNotFoundException("Requested role does not exist");});
        users.save(request.getUser());
        request.approve(reviewer);
        requests.save(request);
        return ApiResponse.success("Role request approved", view(request));
    }

    @PostMapping("/admin/role-requests/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiResponse<Map<String,Object>> reject(@PathVariable UUID id, @RequestParam(required=false) String reason,
                                                    @AuthenticationPrincipal CurrentUserPrincipal principal) {
        RoleRequest request=requests.findById(id).orElseThrow(()->new EntityNotFoundException("Role request not found"));
        if(!"PENDING".equals(request.getStatus())) throw new IllegalArgumentException("Only pending requests can be rejected");
        User reviewer=users.findById(principal.userId()).orElseThrow(()->new EntityNotFoundException("Reviewer not found"));
        request.reject(reviewer, reason == null ? "Request rejected by administrator" : reason.trim());
        requests.save(request);
        return ApiResponse.success("Role request rejected", view(request));
    }

    private Map<String,Object> view(RoleRequest r) {
        return Map.of("id",r.getId(),"userId",r.getUser().getId(),"userName",(r.getUser().getFirstName()+" "+(r.getUser().getLastName()==null?"":r.getUser().getLastName())).trim(),"userEmail",r.getUser().getEmail(),"requestedRole",r.getRequestedRole(),"status",r.getStatus(),"reason",r.getReason()==null?"":r.getReason(),"rejectionReason",r.getRejectionReason()==null?"":r.getRejectionReason());
    }
}
