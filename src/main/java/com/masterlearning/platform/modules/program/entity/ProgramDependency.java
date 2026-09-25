package com.masterlearning.platform.modules.program.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name="program_dependencies", uniqueConstraints=@UniqueConstraint(name="uk_program_dependency_edge", columnNames={"predecessor_id","successor_id"}), indexes={
 @Index(name="idx_program_dependencies_predecessor", columnList="predecessor_id"),
 @Index(name="idx_program_dependencies_successor", columnList="successor_id")})
public class ProgramDependency extends BaseEntity {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="program_id",nullable=false) private Program program;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="predecessor_id",nullable=false) private ProgramMilestone predecessor;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="successor_id",nullable=false) private ProgramMilestone successor;
 @Column(nullable=false,length=20) private String type="BLOCKS";
 protected ProgramDependency(){}
 public ProgramDependency(Program program,ProgramMilestone predecessor,ProgramMilestone successor){this.program=program;this.predecessor=predecessor;this.successor=successor;}
 public UUID getId(){return id;} public Program getProgram(){return program;} public ProgramMilestone getPredecessor(){return predecessor;} public ProgramMilestone getSuccessor(){return successor;} public String getType(){return type;}
}
