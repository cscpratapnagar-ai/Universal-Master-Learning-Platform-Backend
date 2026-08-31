package com.masterlearning.platform.modules.course.entity;
import com.masterlearning.platform.common.entity.BaseEntity; import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="course_modules") public class CourseModule extends BaseEntity {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="course_id",nullable=false) private Course course;
 @Column(nullable=false,length=180) private String title; @Column(name="sort_order",nullable=false) private int sortOrder;
 protected CourseModule(){} public CourseModule(Course c,String t,int o){course=c;title=t;sortOrder=o;}
 public UUID getId(){return id;} public String getTitle(){return title;} public int getSortOrder(){return sortOrder;} public Course getCourse(){return course;}
}