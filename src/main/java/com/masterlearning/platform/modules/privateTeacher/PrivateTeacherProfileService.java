package com.masterlearning.platform.modules.privateTeacher;
import com.masterlearning.platform.modules.privateTeacher.entity.PrivateTeacherProfile; import com.masterlearning.platform.modules.privateTeacher.repository.PrivateTeacherProfileRepository; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.UUID;
@Service public class PrivateTeacherProfileService {
 private final PrivateTeacherProfileRepository repository; public PrivateTeacherProfileService(PrivateTeacherProfileRepository repository){this.repository=repository;}
 @Transactional(readOnly=true) public PrivateTeacherProfileResponse get(UUID id){return repository.findByTeacherId(id).map(PrivateTeacherProfileResponse::from).orElse(null);}
 @Transactional public PrivateTeacherProfileResponse upsert(UUID id,PrivateTeacherProfileRequest r){var p=repository.findByTeacherId(id).orElseGet(()->new PrivateTeacherProfile(id));p.update(r.headline(),r.bio(),r.subjects(),r.teachingModes(),r.languages(),r.hourlyRate(),r.currency()==null?"INR":r.currency().toUpperCase(),r.acceptingLearners());return PrivateTeacherProfileResponse.from(repository.save(p));}
}