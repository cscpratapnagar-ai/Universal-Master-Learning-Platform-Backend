package com.masterlearning.platform.modules.assessment.entity;
import com.masterlearning.platform.common.entity.BaseEntity; import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="question_options") public class QuestionOption extends BaseEntity {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id; @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="question_id",nullable=false) private Question question;
 @Column(nullable=false,length=2000) private String optionText; @Column(nullable=false) private boolean correct;
 protected QuestionOption(){} public QuestionOption(Question q,String t,boolean c){question=q;optionText=t;correct=c;}
 public UUID getId(){return id;} public String getOptionText(){return optionText;} public boolean isCorrect(){return correct;}
}