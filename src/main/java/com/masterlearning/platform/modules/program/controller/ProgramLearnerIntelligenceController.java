package com.masterlearning.platform.modules.program.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.organization.security.OrganizationAuthorizationService;
import com.masterlearning.platform.modules.program.repository.ProgramRepository;
import com.masterlearning.platform.modules.program.service.ProgramLearnerIntelligenceService;
import com.masterlearning.platform.modules.program.service.ProgramInterventionService;
import com.masterlearning.platform.modules.program.dto.request.CreateProgramInterventionActionRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/programs")
public class ProgramLearnerIntelligenceController {
    private final ProgramRepository programs;
    private final OrganizationAuthorizationService authorization;
    private final ProgramLearnerIntelligenceService intelligence;
    private final ProgramInterventionService interventionService;

    public ProgramLearnerIntelligenceController(ProgramRepository programs,
                                                 OrganizationAuthorizationService authorization,
                                                 ProgramLearnerIntelligenceService intelligence,
                                                 ProgramInterventionService interventionService) {
        this.programs = programs;
        this.authorization = authorization;
        this.intelligence = intelligence;
        this.interventionService = interventionService;
    }

    @GetMapping("/{programId}/intelligence")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','ORG_ADMIN','INSTRUCTOR','TEACHER')")
    public ApiResponse<Map<String,Object>> intelligence(@PathVariable UUID programId) {
        var program = programs.findWithOrganizationById(programId)
                .orElseThrow(() -> new NoSuchElementException("Program not found"));
        UUID organizationId = program.getOrganization() == null ? null : program.getOrganization().getId();
        boolean superAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication() != null
                && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()));

        if (!superAdmin && (organizationId == null || !authorization.canAccessOrganization(organizationId))) {
            throw new AccessDeniedException("You do not have access to this organization");
        }
        return ApiResponse.success("Learner intelligence retrieved", intelligence.analyze(programId));
    }
    @GetMapping("/{programId}/interventions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','ORG_ADMIN','INSTRUCTOR','TEACHER')")
    public ApiResponse<Map<String,Object>> interventions(@PathVariable UUID programId) {
        var program = programs.findWithOrganizationById(programId)
                .orElseThrow(() -> new NoSuchElementException("Program not found"));
        UUID organizationId = program.getOrganization() == null ? null : program.getOrganization().getId();
        boolean superAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication() != null
                && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
        if (!superAdmin && (organizationId == null || !authorization.canAccessOrganization(organizationId))) {
            throw new AccessDeniedException("You do not have access to this organization");
        }
        return ApiResponse.success("Learner intervention queue retrieved", intelligence.interventionQueue(programId));
    }


    @PostMapping("/{programId}/interventions/{userId}/actions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','ORG_ADMIN','INSTRUCTOR','TEACHER')")
    public ApiResponse<Map<String,Object>> createInterventionAction(@PathVariable UUID programId,
                                                                     @PathVariable UUID userId,
                                                                     @Valid @RequestBody CreateProgramInterventionActionRequest request) {
        assertOrganizationAccess(programId);
        return ApiResponse.success("Intervention action recorded", interventionService.create(programId,userId,request));
    }

    @GetMapping("/{programId}/interventions/{userId}/actions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','ORG_ADMIN','INSTRUCTOR','TEACHER')")
    public ApiResponse<List<Map<String,Object>>> interventionHistory(@PathVariable UUID programId,@PathVariable UUID userId) {
        assertOrganizationAccess(programId);
        return ApiResponse.success("Intervention history retrieved", interventionService.history(programId,userId));
    }

    @PutMapping("/{programId}/interventions/actions/{actionId}/resolve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','ORG_ADMIN','INSTRUCTOR','TEACHER')")
    public ApiResponse<Map<String,Object>> resolveInterventionAction(@PathVariable UUID programId,@PathVariable UUID actionId) {
        assertOrganizationAccess(programId);
        return ApiResponse.success("Intervention action resolved", interventionService.resolve(programId,actionId));
    }

    private void assertOrganizationAccess(UUID programId) {
        var program = programs.findWithOrganizationById(programId)
                .orElseThrow(() -> new NoSuchElementException("Program not found"));
        UUID organizationId = program.getOrganization() == null ? null : program.getOrganization().getId();
        boolean superAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication() != null
                && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
        if (!superAdmin && (organizationId == null || !authorization.canAccessOrganization(organizationId))) {
            throw new AccessDeniedException("You do not have access to this organization");
        }
    }
}

