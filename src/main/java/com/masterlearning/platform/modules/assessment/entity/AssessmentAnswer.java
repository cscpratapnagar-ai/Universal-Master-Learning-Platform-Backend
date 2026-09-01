package com.masterlearning.platform.modules.assessment.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name="assessment_answers")
public class AssessmentAnswer extends BaseEntity {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="attempt_id",nullable=false) private AssessmentAttempt attempt;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="question_id",nullable=false) private Question question;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="selected_option_id") private QuestionOption selectedOption;
    @Column(nullable=false) private boolean correct;
    @Column(nullable=false) private int pointsAwarded;

    protected AssessmentAnswer(){}
    public AssessmentAnswer(AssessmentAttempt attempt, Question question, QuestionOption selectedOption, boolean correct, int pointsAwarded){
        this.attempt=attempt; this.question=question; this.selectedOption=selectedOption; this.correct=correct; this.pointsAwarded=pointsAwarded;
    }
    public UUID getId(){return id;} public boolean isCorrect(){return correct;} public int getPointsAwarded(){return pointsAwarded;}
}