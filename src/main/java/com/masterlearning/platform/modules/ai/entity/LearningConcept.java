package com.masterlearning.platform.modules.ai.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import com.masterlearning.platform.modules.course.entity.Course;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "learning_concepts")
public class LearningConcept extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private LearningConcept parent;

    @Column(nullable = false, length = 120)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "concept_type", nullable = false, length = 20)
    private String conceptType = "CONCEPT";

    @Column(nullable = false)
    private boolean active = true;

    protected LearningConcept() {}

    public LearningConcept(Course course, LearningConcept parent, String code, String name,
                           String description, String conceptType) {
        this.course = course;
        this.parent = parent;
        this.code = code;
        this.name = name;
        this.description = description;
        this.conceptType = conceptType == null || conceptType.isBlank() ? "CONCEPT" : conceptType;
    }

    public UUID getId() { return id; }
    public Course getCourse() { return course; }
    public LearningConcept getParent() { return parent; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getConceptType() { return conceptType; }
    public boolean isActive() { return active; }
    public void deactivate() { active = false; }
}
