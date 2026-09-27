package com.masterlearning.platform.modules.program.service;

import com.masterlearning.platform.modules.program.dto.request.CreateProgramInterventionActionRequest;
import com.masterlearning.platform.modules.program.entity.*;
import com.masterlearning.platform.modules.program.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class ProgramInterventionService {
    private final ProgramRepository programs;
    private final ProgramEnrollmentRepository enrollments;
    private final ProgramInterventionActionRepository actions;
    private final ProgramActivityRepository activities;

    public ProgramInterventionService(ProgramRepository programs, ProgramEnrollmentRepository enrollments,
                                      ProgramInterventionActionRepository actions, ProgramActivityRepository activities) {
        this.programs=programs; this.enrollments=enrollments; this.actions=actions; this.activities=activities;
    }

    @Transactional
    public Map<String,Object> create(UUID programId, UUID userId, CreateProgramInterventionActionRequest request) {
        var program=programs.findWithOrganizationById(programId).orElseThrow(()->new NoSuchElementException("Program not found"));
        var enrollment=enrollments.findByProgramIdAndUserId(programId,userId).orElseThrow(()->new NoSuchElementException("Learner is not assigned to this project"));
        if ("CANCELLED".equals(enrollment.getStatus())) throw new IllegalStateException("Cancelled learner assignment cannot receive intervention actions");
        String actor=currentActor();
        var action=actions.save(new ProgramInterventionAction(program,enrollment.getUser(),request.actionType().trim().toUpperCase(),request.note(),actor));
        activities.save(new ProgramActivity(program,"INTERVENTION_ACTION_CREATED",
                request.actionType().trim().toUpperCase()+" for "+enrollment.getUser().getEmail()+
                        (request.note()==null||request.note().isBlank()?"":" · "+request.note()),actor));
        return data(action);
    }

    @Transactional
    public Map<String,Object> resolve(UUID programId, UUID actionId) {
        var action=actions.findById(actionId).orElseThrow(()->new NoSuchElementException("Intervention action not found"));
        if (!action.getProgram().getId().equals(programId)) throw new NoSuchElementException("Intervention action not found");
        if ("RESOLVED".equals(action.getStatus())) return data(action);
        action.resolve();
        String actor=currentActor();
        activities.save(new ProgramActivity(action.getProgram(),"INTERVENTION_ACTION_RESOLVED",
                action.getActionType()+" for "+action.getUser().getEmail(),actor));
        return data(action);
    }

    @Transactional(readOnly=true)
    public List<Map<String,Object>> history(UUID programId, UUID userId) {
        return actions.findByProgramIdAndUserIdOrderByCreatedAtDesc(programId,userId).stream().map(this::data).toList();
    }

    private Map<String,Object> data(ProgramInterventionAction a) {
        Map<String,Object> m=new LinkedHashMap<>();
        m.put("id",a.getId()); m.put("programId",a.getProgram().getId()); m.put("userId",a.getUser().getId());
        m.put("userEmail",a.getUser().getEmail()); m.put("actionType",a.getActionType()); m.put("status",a.getStatus());
        m.put("note",a.getNote()); m.put("actor",a.getActor()); m.put("createdAt",a.getCreatedAt()); m.put("resolvedAt",a.getResolvedAt());
        return m;
    }
    private String currentActor(){
        var a=SecurityContextHolder.getContext().getAuthentication();
        return a==null||a.getName()==null?"system":a.getName();
    }
}