package com.masterlearning.platform.modules.privateTeacher;

import com.masterlearning.platform.modules.privateTeacher.entity.PrivateTeacherAvailability;
import com.masterlearning.platform.modules.privateTeacher.entity.PrivateTeacherSession;
import com.masterlearning.platform.modules.privateTeacher.repository.PrivateTeacherAvailabilityRepository;
import com.masterlearning.platform.modules.privateTeacher.repository.PrivateTeacherSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;
import java.util.UUID;

@Service
public class PrivateTeacherSessionService {
 private final PrivateTeacherSessionRepository sessions;
 private final PrivateTeacherAvailabilityRepository availability;

 public PrivateTeacherSessionService(PrivateTeacherSessionRepository sessions,PrivateTeacherAvailabilityRepository availability){
  this.sessions=sessions;this.availability=availability;
 }

 @Transactional(readOnly=true)
 public List<PrivateTeacherSessionResponse> mine(UUID userId){
  return java.util.stream.Stream.concat(teacherMine(userId).stream(),learnerMine(userId).stream()).distinct().sorted(java.util.Comparator.comparing(PrivateTeacherSessionResponse::startsAt).reversed()).toList();
 }

 @Transactional(readOnly=true)
 public List<PrivateTeacherSessionResponse> teacherMine(UUID teacherId){
  return sessions.findByTeacherIdOrderByStartsAtDesc(teacherId).stream().map(PrivateTeacherSessionResponse::from).toList();
 }

 @Transactional(readOnly=true)
 public List<PrivateTeacherSessionResponse> learnerMine(UUID learnerId){
  return sessions.findByLearnerIdOrderByStartsAtDesc(learnerId).stream().map(PrivateTeacherSessionResponse::from).toList();
 }

 @Transactional
 public PrivateTeacherSessionResponse request(UUID learnerId,PrivateTeacherSessionRequest r){
  if(learnerId.equals(r.teacherId())) throw new IllegalArgumentException("Learner cannot book a session with themselves");
  if(!r.endsAt().isAfter(r.startsAt())) throw new IllegalArgumentException("Session end must be after start");
  if(r.startsAt().isBefore(Instant.now())) throw new IllegalArgumentException("Session must start in the future");
  if(sessions.countTeacherOverlap(r.teacherId(),r.startsAt(),r.endsAt())>0) throw new IllegalArgumentException("Teacher already has an overlapping session");
  validateAvailability(r.teacherId(),r.startsAt(),r.endsAt());
  try {
   return PrivateTeacherSessionResponse.from(sessions.save(new PrivateTeacherSession(r.teacherId(),learnerId,r.startsAt(),r.endsAt(),r.timezone()==null?"UTC":r.timezone(),r.topic(),r.notes())));
  } catch (org.springframework.dao.DataIntegrityViolationException ex) {
   throw new IllegalArgumentException("Teacher already has an overlapping session");
  }
 }

 @Transactional
 public PrivateTeacherSessionResponse confirm(UUID actorId,UUID id){
  var s=get(id); requireTeacher(actorId,s); s.confirm(); return PrivateTeacherSessionResponse.from(s);
 }

 @Transactional
 public PrivateTeacherSessionResponse cancel(UUID actorId,UUID id){
  var s=get(id); requireParticipant(actorId,s); s.cancel(); return PrivateTeacherSessionResponse.from(s);
 }

 @Transactional
 public PrivateTeacherSessionResponse complete(UUID actorId,UUID id){
  var s=get(id); requireTeacher(actorId,s); s.complete(); return PrivateTeacherSessionResponse.from(s);
 }

 private void validateAvailability(UUID teacherId,Instant startsAt,Instant endsAt){
  var slots=availability.findByTeacherIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(teacherId);
  if(slots.isEmpty()) throw new IllegalArgumentException("Teacher has no active availability");
  boolean covered=slots.stream().anyMatch(slot -> covers(slot,startsAt,endsAt));
  if(!covered) throw new IllegalArgumentException("Requested session is outside the teacher's availability");
 }

 private boolean covers(PrivateTeacherAvailability slot,Instant start,Instant end){
  try{
   var zone=ZoneId.of(slot.getTimezone());
   var localStart=start.atZone(zone);
   var localEnd=end.atZone(zone);
   return localStart.getDayOfWeek()==slot.getDayOfWeek()
       && localStart.toLocalDate().equals(localEnd.toLocalDate())
       && !localStart.toLocalTime().isBefore(slot.getStartTime())
       && !localEnd.toLocalTime().isAfter(slot.getEndTime());
  }catch(DateTimeException ex){ return false; }
 }

 private void requireTeacher(UUID actorId,PrivateTeacherSession s){
  if(!s.getTeacherId().equals(actorId)) throw new SecurityException("Only the assigned teacher can perform this action");
 }
 private void requireParticipant(UUID actorId,PrivateTeacherSession s){
  if(!s.getTeacherId().equals(actorId) && !s.getLearnerId().equals(actorId)) throw new SecurityException("Only the teacher or learner can perform this action");
 }
 private PrivateTeacherSession get(UUID id){return sessions.findById(id).orElseThrow(()->new IllegalArgumentException("Session not found"));}
}
