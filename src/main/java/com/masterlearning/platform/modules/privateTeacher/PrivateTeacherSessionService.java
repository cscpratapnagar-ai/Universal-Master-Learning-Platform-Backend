package com.masterlearning.platform.modules.privateTeacher;
import com.masterlearning.platform.modules.privateTeacher.entity.PrivateTeacherSession; import com.masterlearning.platform.modules.privateTeacher.repository.PrivateTeacherSessionRepository; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.time.Instant; import java.util.UUID;
import java.util.List;
@Service public class PrivateTeacherSessionService {
 private final PrivateTeacherSessionRepository sessions; public PrivateTeacherSessionService(PrivateTeacherSessionRepository s){sessions=s;}
 @Transactional(readOnly=true) public List<PrivateTeacherSessionResponse> mine(UUID userId){
  var teacher=sessions.findByTeacherIdOrderByStartsAtDesc(userId).stream().map(PrivateTeacherSessionResponse::from).toList();
  var learner=sessions.findByLearnerIdOrderByStartsAtDesc(userId).stream().map(PrivateTeacherSessionResponse::from).toList();
  return java.util.stream.Stream.concat(teacher.stream(),learner.stream()).distinct().sorted(java.util.Comparator.comparing(PrivateTeacherSessionResponse::startsAt).reversed()).toList();
 }
 @Transactional public PrivateTeacherSessionResponse request(UUID learnerId,PrivateTeacherSessionRequest r){
  if(!r.endsAt().isAfter(r.startsAt())) throw new IllegalArgumentException("Session end must be after start");
  if(r.startsAt().isBefore(Instant.now())) throw new IllegalArgumentException("Session must start in the future");
  if(sessions.countTeacherOverlap(r.teacherId(),r.startsAt(),r.endsAt())>0) throw new IllegalArgumentException("Teacher already has an overlapping session");
  return PrivateTeacherSessionResponse.from(sessions.save(new PrivateTeacherSession(r.teacherId(),learnerId,r.startsAt(),r.endsAt(),r.timezone()==null?"UTC":r.timezone(),r.topic(),r.notes())));
 }
 @Transactional public PrivateTeacherSessionResponse confirm(UUID id){var s=get(id);s.confirm();return PrivateTeacherSessionResponse.from(s);}
 @Transactional public PrivateTeacherSessionResponse cancel(UUID id){var s=get(id);s.cancel();return PrivateTeacherSessionResponse.from(s);}
 @Transactional public PrivateTeacherSessionResponse complete(UUID id){var s=get(id);s.complete();return PrivateTeacherSessionResponse.from(s);}
 private PrivateTeacherSession get(UUID id){return sessions.findById(id).orElseThrow(()->new IllegalArgumentException("Session not found"));}
}