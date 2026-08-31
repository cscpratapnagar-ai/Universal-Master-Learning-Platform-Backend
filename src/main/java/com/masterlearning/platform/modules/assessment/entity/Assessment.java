package com.masterlearning.platform.modules.assessment.entity;
import com.masterlearning.platform.common.entity.BaseEntity; import com.masterlearning.platform.modules.course.entity.Course; import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="assessments") public class Assessment extends BaseEntity {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="course_id",nullable=false) private Course course;
 @Column(nullable=false,length=180) private String title; @Column(nullable=false) private int passingScore=60;
 protected Assessment(){} public Assessment(Course c,String t,int p){course=c;title=t;passingScore=p;}
 public UUID getId(){return id;} public Course getCourse(){return course;} public String getTitle(){return title;} public int getPassingScore(){return passingScore;}
}