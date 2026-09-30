package com.masterlearning.platform.modules.privateTeacher.entity;
import com.masterlearning.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.util.UUID;
@Entity @Table(name="private_teacher_profiles")
public class PrivateTeacherProfile extends BaseEntity {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(name="teacher_id",nullable=false,unique=true) private UUID teacherId;
 @Column(length=180) private String headline; @Column(length=4000) private String bio;
 @Column(length=2000) private String subjects; @Column(name="teaching_modes",length=1000) private String teachingModes;
 @Column(length=1000) private String languages; @Column(name="hourly_rate",precision=12,scale=2) private BigDecimal hourlyRate;
 @Column(nullable=false,length=3) private String currency="INR"; @Column(name="accepting_learners",nullable=false) private boolean acceptingLearners;
 protected PrivateTeacherProfile(){} public PrivateTeacherProfile(UUID teacherId){this.teacherId=teacherId;}
 public UUID getId(){return id;} public UUID getTeacherId(){return teacherId;} public String getHeadline(){return headline;} public String getBio(){return bio;}
 public String getSubjects(){return subjects;} public String getTeachingModes(){return teachingModes;} public String getLanguages(){return languages;} public BigDecimal getHourlyRate(){return hourlyRate;}
 public String getCurrency(){return currency;} public boolean isAcceptingLearners(){return acceptingLearners;}
 public void update(String h,String b,String s,String m,String l,BigDecimal r,String c,boolean a){headline=h;bio=b;subjects=s;teachingModes=m;languages=l;hourlyRate=r;currency=c;acceptingLearners=a;}
}