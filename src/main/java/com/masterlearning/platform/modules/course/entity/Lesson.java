package com.masterlearning.platform.modules.course.entity;
import com.masterlearning.platform.common.entity.BaseEntity; import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="lessons") public class Lesson extends BaseEntity {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="module_id",nullable=false) private CourseModule module;
 @Column(nullable=false,length=220) private String title; @Column(length=30) private String contentType="TEXT"; @Column(length=4000) private String content; @Column(name="sort_order",nullable=false) private int sortOrder;
 protected Lesson(){} public Lesson(CourseModule m,String t,String type,String c,int o){module=m;title=t;contentType=type;content=c;sortOrder=o;}
 public UUID getId(){return id;} public String getTitle(){return title;} public String getContentType(){return contentType;} public String getContent(){return content;} public int getSortOrder(){return sortOrder;}
}