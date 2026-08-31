package com.masterlearning.platform.modules.assessment.entity;
import com.masterlearning.platform.common.entity.BaseEntity; import com.masterlearning.platform.modules.user.entity.User; import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="assessment_attempts") public class AssessmentAttempt extends BaseEntity {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id; @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="assessment_id",nullable=false) private Assessment assessment; @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false) private User user;
 @Column(nullable=false) private int score; @Column(nullable=false) private boolean passed; private Instant submittedAt=Instant.now();
 protected AssessmentAttempt(){} public AssessmentAttempt(Assessment a,User u,int s,boolean p){assessment=a;user=u;score=s;passed=p;}
 public UUID getId(){return id;} public int getScore(){return score;} public boolean isPassed(){return passed;}
}