package com.masterlearning.platform.modules.assessment.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import com.masterlearning.platform.modules.user.entity.User;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "assessment_evaluator_assignments",
       uniqueConstraints = @UniqueConstraint(name = "uk_assessment_evaluator_assignment",
               columnNames = {"assessment_id", "evaluator_id"}))
public class AssessmentEvaluatorAssignment extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private User evaluator;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_by", nullable = false)
    private User assignedBy;

    protected AssessmentEvaluatorAssignment() {}

    public AssessmentEvaluatorAssignment(Assessment assessment, User evaluator, User assignedBy) {
        this.assessment = assessment;
        this.evaluator = evaluator;
        this.assignedBy = assignedBy;
    }

    public UUID getId() { return id; }
    public Assessment getAssessment() { return assessment; }
    public User getEvaluator() { return evaluator; }
    public User getAssignedBy() { return assignedBy; }
}
