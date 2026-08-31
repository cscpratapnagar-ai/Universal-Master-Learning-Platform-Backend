package com.masterlearning.platform.modules.course.entity;
import com.masterlearning.platform.common.entity.BaseEntity; import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="lesson_progress",uniqueConstraints=@UniqueConstraint(columnNames={"enrollment_id","lesson_id"})) public class LessonProgress extends BaseEntity {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="enrollment_id",nullable=false) private Enrollment enrollment;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="lesson_id",nullable=false) private Lesson lesson;
 @Column(nullable=false) private boolean completed=false; private Instant completedAt;
 protected LessonProgress(){} public LessonProgress(Enrollment e,Lesson l){enrollment=e;lesson=l;}
 public UUID getId(){return id;} public boolean isCompleted(){return completed;} public void complete(){completed=true;completedAt=Instant.now();}
}