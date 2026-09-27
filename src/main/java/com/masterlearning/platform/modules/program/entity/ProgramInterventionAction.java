package com.masterlearning.platform.modules.program.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="program_intervention_actions", indexes={
    @Index(name="idx_intervention_program_user", columnList="program_id,user_id,created_at")
})
public class ProgramInterventionAction extends BaseEntity {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="program_id",nullable=false) private Program program;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="user_id",nullable=false) private com.masterlearning.platform.modules.user.entity.User user;
    @Column(name="action_type",nullable=false,length=60) private String actionType;
    @Column(nullable=false,length=20) private String status="OPEN";
    @Column(length=3000) private String note;
    @Column(name="actor",nullable=false,length=180) private String actor;
    private Instant resolvedAt;

    protected ProgramInterventionAction() {}
    public ProgramInterventionAction(Program program, com.masterlearning.platform.modules.user.entity.User user,
                                     String actionType, String note, String actor) {
        this.program=program; this.user=user; this.actionType=actionType; this.note=note; this.actor=actor;
    }
    public UUID getId(){return id;} public Program getProgram(){return program;} public com.masterlearning.platform.modules.user.entity.User getUser(){return user;}
    public String getActionType(){return actionType;} public String getStatus(){return status;} public String getNote(){return note;} public String getActor(){return actor;} public Instant getResolvedAt(){return resolvedAt;}
    public void resolve(){status="RESOLVED"; resolvedAt=Instant.now();}
}