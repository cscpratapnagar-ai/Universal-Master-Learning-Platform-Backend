package com.masterlearning.platform.modules.course.entity;
import com.masterlearning.platform.common.entity.BaseEntity; import com.masterlearning.platform.modules.user.entity.User; import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="course_enrollments",uniqueConstraints=@UniqueConstraint(columnNames={"course_id","user_id"})) public class Enrollment extends BaseEntity {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="course_id",nullable=false) private Course course;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false) private User user;
 @Column(nullable=false) private int progressPercent=0; private Instant completedAt;
 protected Enrollment(){} public Enrollment(Course c,User u){course=c;user=u;}
 public UUID getId(){return id;} public int getProgressPercent(){return progressPercent;} public void updateProgress(int p){progressPercent=Math.max(0,Math.min(100,p));if(progressPercent==100)completedAt=Instant.now();}
}