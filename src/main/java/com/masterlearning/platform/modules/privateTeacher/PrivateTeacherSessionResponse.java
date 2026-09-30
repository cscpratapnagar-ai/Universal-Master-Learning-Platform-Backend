package com.masterlearning.platform.modules.privateTeacher;
import com.masterlearning.platform.modules.privateTeacher.entity.PrivateTeacherSession; import java.time.Instant; import java.util.UUID;
public record PrivateTeacherSessionResponse(UUID id,UUID teacherId,UUID learnerId,Instant startsAt,Instant endsAt,String timezone,String status,String topic,String notes){
 public static PrivateTeacherSessionResponse from(PrivateTeacherSession s){return new PrivateTeacherSessionResponse(s.getId(),s.getTeacherId(),s.getLearnerId(),s.getStartsAt(),s.getEndsAt(),s.getTimezone(),s.getStatus().name(),s.getTopic(),s.getNotes());}
}