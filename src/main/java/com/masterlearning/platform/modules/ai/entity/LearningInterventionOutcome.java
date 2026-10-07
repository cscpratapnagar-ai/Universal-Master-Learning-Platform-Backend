package com.masterlearning.platform.modules.ai.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import com.masterlearning.platform.modules.course.entity.Enrollment;
import com.masterlearning.platform.modules.user.entity.User;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "learning_intervention_outcomes")
public class LearningInterventionOutcome extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="enrollment_id", nullable=false) private Enrollment enrollment;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="user_id", nullable=false) private User user;
    @Column(name="intervention_type", nullable=false, length=60) private String interventionType;
    @Column(name="target_lesson_id") private UUID targetLessonId;
    @Column(nullable=false, length=40) private String outcome;
    @Column(name="mastery_delta") private Double masteryDelta;
    @Column(columnDefinition="TEXT") private String notes;
    protected LearningInterventionOutcome() {}
    public LearningInterventionOutcome(Enrollment enrollment, User user, String interventionType, UUID targetLessonId,
                                       String outcome, Double masteryDelta, String notes) {
        this.enrollment=enrollment; this.user=user; this.interventionType=interventionType;
        this.targetLessonId=targetLessonId; this.outcome=outcome; this.masteryDelta=masteryDelta; this.notes=notes;
    }
    public UUID getId(){return id;} public Enrollment getEnrollment(){return enrollment;} public User getUser(){return user;}
    public String getInterventionType(){return interventionType;} public UUID getTargetLessonId(){return targetLessonId;}
    public String getOutcome(){return outcome;} public Double getMasteryDelta(){return masteryDelta;} public String getNotes(){return notes;}
}