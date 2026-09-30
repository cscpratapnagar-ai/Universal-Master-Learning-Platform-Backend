package com.masterlearning.platform.modules.privateTeacher.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="private_teacher_sessions")
public class PrivateTeacherSession extends BaseEntity {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(name="teacher_id",nullable=false) private UUID teacherId;
 @Column(name="learner_id",nullable=false) private UUID learnerId;
 @Column(name="starts_at",nullable=false) private Instant startsAt;
 @Column(name="ends_at",nullable=false) private Instant endsAt;
 @Column(nullable=false,length=80) private String timezone="UTC";
 @Column(nullable=false,length=20) @Enumerated(EnumType.STRING) private Status status=Status.REQUESTED;
 @Column(length=500) private String topic;
 @Column(length=3000) private String notes;
 protected PrivateTeacherSession(){}
 public PrivateTeacherSession(UUID t,UUID l,Instant s,Instant e,String z,String topic,String notes){teacherId=t;learnerId=l;startsAt=s;endsAt=e;timezone=z;this.topic=topic;this.notes=notes;}
 public UUID getId(){return id;} public UUID getTeacherId(){return teacherId;} public UUID getLearnerId(){return learnerId;} public Instant getStartsAt(){return startsAt;} public Instant getEndsAt(){return endsAt;} public String getTimezone(){return timezone;} public Status getStatus(){return status;}
 public String getTopic(){return topic;} public String getNotes(){return notes;}
 public void confirm(){require(Status.REQUESTED);status=Status.CONFIRMED;}
 public void cancel(){if(status==Status.CANCELLED||status==Status.COMPLETED) throw new IllegalStateException("Session cannot be cancelled in its current state");status=Status.CANCELLED;}
 public void complete(){require(Status.CONFIRMED);status=Status.COMPLETED;}
 private void require(Status expected){if(status!=expected) throw new IllegalStateException("Session must be "+expected+" before this action");}
 public enum Status{REQUESTED,CONFIRMED,CANCELLED,COMPLETED}
}
