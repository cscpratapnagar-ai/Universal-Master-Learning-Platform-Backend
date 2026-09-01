package com.masterlearning.platform.modules.assessment.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import com.masterlearning.platform.modules.course.entity.Course;
import com.masterlearning.platform.modules.course.entity.CourseModule;
import com.masterlearning.platform.modules.course.entity.Lesson;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "assessments")
public class Assessment extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "module_id")
    private CourseModule module;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "lesson_id")
    private Lesson lesson;
    @Column(nullable = false, length = 30)
    private String assessmentLevel = "COURSE";
    @Column(nullable = false, length = 180)
    private String title;
    @Column(nullable = false)
    private int passingScore = 60;
    @Column(nullable = false)
    private int maxAttempts = 3;

    protected Assessment() {}
    public Assessment(Course course, String title, int passingScore) { this(course, null, null, "COURSE", title, passingScore, 3); }
    public Assessment(Course course, CourseModule module, Lesson lesson, String assessmentLevel, String title, int passingScore) { this(course,module,lesson,assessmentLevel,title,passingScore,3); }
    public Assessment(Course course, CourseModule module, Lesson lesson, String assessmentLevel, String title, int passingScore, int maxAttempts) {
        this.course=course; this.module=module; this.lesson=lesson; this.assessmentLevel=assessmentLevel;
        this.title=title; this.passingScore=passingScore; this.maxAttempts=maxAttempts;
    }
    public UUID getId(){return id;} public Course getCourse(){return course;} public CourseModule getModule(){return module;}
    public Lesson getLesson(){return lesson;} public String getAssessmentLevel(){return assessmentLevel;}
    public String getTitle(){return title;} public int getPassingScore(){return passingScore;} public int getMaxAttempts(){return maxAttempts;}
}