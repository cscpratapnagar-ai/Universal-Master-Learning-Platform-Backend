package com.masterlearning.platform.modules.assessment.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "assessment_questions")
public class Question extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Column(nullable = false, length = 3000)
    private String questionText;

    @Column(nullable = false, length = 20)
    private String questionType = "SINGLE_CHOICE";

    @Column(nullable = false)
    private int points = 1;

    @Column(name = "difficulty_level", nullable = false, length = 20)
    private String difficultyLevel = "MEDIUM";

    protected Question() {}

    public Question(Assessment assessment, String questionText, String questionType, int points) {
        this(assessment, questionText, questionType, points, "MEDIUM");
    }

    public Question(Assessment assessment, String questionText, String questionType, int points, String difficultyLevel) {
        this.assessment = assessment;
        this.questionText = questionText;
        this.questionType = questionType;
        this.points = points;
        this.difficultyLevel = difficultyLevel == null ? "MEDIUM" : difficultyLevel.toUpperCase();
    }

    public UUID getId() { return id; }
    public String getQuestionText() { return questionText; }
    public String getQuestionType() { return questionType; }
    public int getPoints() { return points; }
    public String getDifficultyLevel() { return difficultyLevel; }
}
