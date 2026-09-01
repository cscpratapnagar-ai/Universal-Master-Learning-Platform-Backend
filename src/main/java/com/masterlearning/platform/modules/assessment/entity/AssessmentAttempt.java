package com.masterlearning.platform.modules.assessment.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import com.masterlearning.platform.modules.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="assessment_attempts",
       uniqueConstraints=@UniqueConstraint(name="uk_assessment_attempt_number", columnNames={"assessment_id","user_id","attempt_number"}))
public class AssessmentAttempt extends BaseEntity {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="assessment_id",nullable=false) private Assessment assessment;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false) private User user;
    @Column(name="attempt_number",nullable=false) private int attemptNumber;
    @Column(nullable=false) private int score;
    @Column(nullable=false) private boolean passed;
    @Column(nullable=false,length=30) private String masteryLevel;
    @Column(nullable=false) private Instant submittedAt=Instant.now();

    protected AssessmentAttempt(){}
    public AssessmentAttempt(Assessment a, User u, int attemptNumber, int score, boolean passed, String masteryLevel){
        this.assessment=a; this.user=u; this.attemptNumber=attemptNumber; this.score=score; this.passed=passed; this.masteryLevel=masteryLevel; this.submittedAt=Instant.now();
    }
    public UUID getId(){return id;} public int getAttemptNumber(){return attemptNumber;} public int getScore(){return score;}
    public boolean isPassed(){return passed;} public String getMasteryLevel(){return masteryLevel;} public Instant getSubmittedAt(){return submittedAt;}
}