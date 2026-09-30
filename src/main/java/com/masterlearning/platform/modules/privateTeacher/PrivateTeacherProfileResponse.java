package com.masterlearning.platform.modules.privateTeacher;
import com.masterlearning.platform.modules.privateTeacher.entity.PrivateTeacherProfile; import java.math.BigDecimal; import java.util.UUID;
public record PrivateTeacherProfileResponse(UUID id,UUID teacherId,String headline,String bio,String subjects,String teachingModes,String languages,BigDecimal hourlyRate,String currency,boolean acceptingLearners){
 public static PrivateTeacherProfileResponse from(PrivateTeacherProfile p){return new PrivateTeacherProfileResponse(p.getId(),p.getTeacherId(),p.getHeadline(),p.getBio(),p.getSubjects(),p.getTeachingModes(),p.getLanguages(),p.getHourlyRate(),p.getCurrency(),p.isAcceptingLearners());}
}