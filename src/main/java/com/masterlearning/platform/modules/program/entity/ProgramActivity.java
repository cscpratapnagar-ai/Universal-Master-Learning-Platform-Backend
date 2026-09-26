package com.masterlearning.platform.modules.program.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name="program_activity", indexes={
  @Index(name="idx_program_activity_program_created", columnList="program_id,created_at")
})
public class ProgramActivity extends BaseEntity {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="program_id", nullable=false) private Program program;
    @Column(nullable=false, length=60) private String action;
    @Column(length=2000) private String details;
    @Column(name="actor", nullable=false, length=180) private String actor;
    protected ProgramActivity() {}
    public ProgramActivity(Program program,String action,String details,String actor){this.program=program;this.action=action;this.details=details;this.actor=actor;}
    public UUID getId(){return id;} public Program getProgram(){return program;} public String getAction(){return action;} public String getDetails(){return details;} public String getActor(){return actor;}
}