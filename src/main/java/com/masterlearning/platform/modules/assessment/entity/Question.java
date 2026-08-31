package com.masterlearning.platform.modules.assessment.entity;
import com.masterlearning.platform.common.entity.BaseEntity; import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="assessment_questions") public class Question extends BaseEntity {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="assessment_id",nullable=false) private Assessment assessment;
 @Column(nullable=false,length=3000) private String questionText; @Column(nullable=false,length=20) private questionType="SINGLE_CHOICE"; @Column(nullable=false) private int points=1;
 protected Question(){} public Question(Assessment a,String q,String t,int p){assessment=a;questionText=q;questionType=t;points=p;}
 public UUID getId(){return id;} public String getQuestionText(){return questionText;} public String getQuestionType(){return questionType;} public int getPoints(){return points;}
}