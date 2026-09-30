package com.masterlearning.platform.modules.privateTeacher;
import com.masterlearning.platform.modules.privateTeacher.entity.PrivateTeacherProfile;
import com.masterlearning.platform.modules.privateTeacher.repository.PrivateTeacherProfileRepository;
import com.masterlearning.platform.modules.user.entity.User;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal; import java.util.*;
@Service public class PrivateTeacherMatchService {
 private final PrivateTeacherProfileRepository profiles; private final UserRepository users;
 public PrivateTeacherMatchService(PrivateTeacherProfileRepository p,UserRepository u){profiles=p;users=u;}
 @Transactional(readOnly=true)
 public List<PrivateTeacherMatchResponse> match(PrivateTeacherMatchRequest r){
   return profiles.findAll().stream().filter(PrivateTeacherProfile::isAcceptingLearners).map(p->{
     User u=users.findById(p.getTeacherId()).orElse(null); if(u==null||!u.isEnabled()) return null;
     int score=0; score+=contains(p.getSubjects(),r.subject())?40:0; score+=contains(p.getLanguages(),r.language())?25:0;
     score+=contains(p.getTeachingModes(),r.teachingMode())?20:0;
     if(r.maxHourlyRate()!=null && p.getHourlyRate()!=null) score+=p.getHourlyRate().compareTo(r.maxHourlyRate())<=0?15:0;
     return new PrivateTeacherMatchResponse(u.getId(),(u.getFirstName()+" "+Optional.ofNullable(u.getLastName()).orElse("")).trim(),
       p.getHeadline(),p.getSubjects(),p.getLanguages(),p.getTeachingModes(),p.getHourlyRate(),p.getCurrency(),score);
   }).filter(Objects::nonNull).filter(x->x.score()>0).sorted(Comparator.comparingInt(PrivateTeacherMatchResponse::score).reversed()).limit(20).toList();
 }
 private boolean contains(String source,String needle){return needle!=null&&!needle.isBlank()&&source!=null&&Arrays.stream(source.split(",")).map(String::trim).anyMatch(x->x.equalsIgnoreCase(needle.trim()));}
}