package com.masterlearning.platform.modules.program.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "program_milestones", indexes = {
    @Index(name = "idx_program_milestones_program_order", columnList = "program_id,sort_order"),
    @Index(name = "idx_program_milestones_program_status", columnList = "program_id,status")
})
public class ProgramMilestone extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;
    @Column(nullable = false, length = 180)
    private String title;
    @Column(length = 2500)
    private String description;
    @Column(name = "due_date")
    private LocalDate dueDate;
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private ProgramMilestoneStatus status = ProgramMilestoneStatus.PLANNED;

    protected ProgramMilestone() {}
    public ProgramMilestone(Program program, String title, String description, LocalDate dueDate, int sortOrder) {
        this.program = program; this.title = title; this.description = description; this.dueDate = dueDate; this.sortOrder = sortOrder;
    }
    public UUID getId(){return id;}
    public Program getProgram(){return program;}
    public String getTitle(){return title;}
    public String getDescription(){return description;}
    public LocalDate getDueDate(){return dueDate;}
    public int getSortOrder(){return sortOrder;}
    public ProgramMilestoneStatus getStatus(){return status;}
    public void update(String title,String description,LocalDate dueDate,int sortOrder){this.title=title;this.description=description;this.dueDate=dueDate;this.sortOrder=sortOrder;}
    public void start(){require(ProgramMilestoneStatus.PLANNED, "Only planned milestones can start"); status=ProgramMilestoneStatus.IN_PROGRESS;}
    public void complete(){if(status!=ProgramMilestoneStatus.IN_PROGRESS) throw new IllegalStateException("Only in-progress milestones can be completed"); status=ProgramMilestoneStatus.COMPLETED;}
    public void block(){if(status==ProgramMilestoneStatus.COMPLETED || status==ProgramMilestoneStatus.CANCELLED) throw new IllegalStateException("Completed or cancelled milestones cannot be blocked"); status=ProgramMilestoneStatus.BLOCKED;}
    public void reopen(){if(status!=ProgramMilestoneStatus.BLOCKED) throw new IllegalStateException("Only blocked milestones can be reopened"); status=ProgramMilestoneStatus.IN_PROGRESS;}
    public void cancel(){if(status==ProgramMilestoneStatus.COMPLETED) throw new IllegalStateException("Completed milestones cannot be cancelled"); status=ProgramMilestoneStatus.CANCELLED;}
    private void require(ProgramMilestoneStatus expected,String message){if(status!=expected) throw new IllegalStateException(message);}
}
