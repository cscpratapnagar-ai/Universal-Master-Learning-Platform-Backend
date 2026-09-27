package com.masterlearning.platform.modules.program.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.organization.security.OrganizationAuthorizationService;
import com.masterlearning.platform.modules.program.repository.ProgramRepository;
import com.masterlearning.platform.modules.program.service.ProgramLearnerIntelligenceService;
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

    public ProgramLearnerIntelligenceController(ProgramRepository programs,
                                                 OrganizationAuthorizationService authorization,
                                                 ProgramLearnerIntelligenceService intelligence) {
        this.programs = programs;
        this.authorization = authorization;
        this.intelligence = intelligence;
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

}
