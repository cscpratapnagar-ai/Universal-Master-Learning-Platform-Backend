package com.masterlearning.platform.modules.program.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import com.masterlearning.platform.modules.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="program_enrollments",
       uniqueConstraints=@UniqueConstraint(name="uk_program_enrollment_program_user",columnNames={"program_id","user_id"}),
       indexes={
         @Index(name="idx_program_enrollment_program",columnList="program_id"),
         @Index(name="idx_program_enrollment_user",columnList="user_id")
       })
public class ProgramEnrollment extends BaseEntity {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="program_id",nullable=false) private Program program;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
    @Column(nullable=false,length=20) private String status="ACTIVE";
    @Column(nullable=false) private int progressPercent=0;
    private Instant completedAt;
    protected ProgramEnrollment(){}
    public ProgramEnrollment(Program program,User user){this.program=program;this.user=user;}
    public UUID getId(){return id;} public Program getProgram(){return program;} public User getUser(){return user;}
    public String getStatus(){return status;} public int getProgressPercent(){return progressPercent;} public Instant getCompletedAt(){return completedAt;}
    public void updateProgress(int value){progressPercent=Math.max(0,Math.min(100,value)); if(progressPercent==100){status="COMPLETED";if(completedAt==null)completedAt=Instant.now();}else if(!"CANCELLED".equals(status)){status="ACTIVE";completedAt=null;}}
    public void cancel(){if("COMPLETED".equals(status))throw new IllegalStateException("Completed program enrollment cannot be cancelled");status="CANCELLED";}
}