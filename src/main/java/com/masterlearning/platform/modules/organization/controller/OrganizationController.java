package com.masterlearning.platform.modules.organization.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.organization.dto.request.*;
import com.masterlearning.platform.modules.organization.dto.response.*;
import com.masterlearning.platform.modules.organization.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/v1/organizations")
public class OrganizationController {
 private final OrganizationService service;
 public OrganizationController(OrganizationService service){this.service=service;}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('SUPER_ADMIN')") public ApiResponse<OrganizationResponse> create(@Valid @RequestBody CreateOrganizationRequest r){return ApiResponse.success("Organization created successfully",service.create(r));}
 @GetMapping @PreAuthorize("hasRole('SUPER_ADMIN')") public ApiResponse<List<OrganizationResponse>> getAll(){return ApiResponse.success("Organizations retrieved successfully",service.getAll());}
 @GetMapping("/me") public ApiResponse<List<OrganizationResponse>> mine(){return ApiResponse.success("Organizations retrieved successfully",service.getCurrentUserOrganizations());}
 @GetMapping("/{id}") @PreAuthorize("hasRole('SUPER_ADMIN')") public ApiResponse<OrganizationResponse> get(@PathVariable UUID id){return ApiResponse.success("Organization retrieved successfully",service.getById(id));}
 @PutMapping("/{id}") @PreAuthorize("hasRole('SUPER_ADMIN')") public ApiResponse<OrganizationResponse> update(@PathVariable UUID id,@Valid @RequestBody UpdateOrganizationRequest r){return ApiResponse.success("Organization updated successfully",service.update(id,r));}
 @DeleteMapping("/{id}") @PreAuthorize("hasRole('SUPER_ADMIN')") public ApiResponse<Void> deactivate(@PathVariable UUID id){service.deactivate(id);return ApiResponse.success("Organization deactivated successfully",null);}
 @PostMapping("/{id}/members") @PreAuthorize("hasRole('SUPER_ADMIN')") public ApiResponse<Void> addMember(@PathVariable UUID id,@Valid @RequestBody AddOrganizationMemberRequest r){service.addMember(id,r);return ApiResponse.success("Member added successfully",null);}
 @GetMapping("/{id}/members") @PreAuthorize("hasRole('SUPER_ADMIN')") public ApiResponse<List<OrganizationMemberResponse>> members(@PathVariable UUID id){return ApiResponse.success("Members retrieved successfully",service.getMembers(id));}
}