package com.masterlearning.platform.modules.privateTeacher.repository;
import com.masterlearning.platform.modules.privateTeacher.entity.PrivateTeacherSession; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.jpa.repository.Query; import org.springframework.data.repository.query.Param; import java.time.Instant; import java.util.UUID;
import java.util.List;
public interface PrivateTeacherSessionRepository extends JpaRepository<PrivateTeacherSession,UUID>{
 @Query("select count(s) from PrivateTeacherSession s where s.teacherId=:teacherId and s.status in (com.masterlearning.platform.modules.privateTeacher.entity.PrivateTeacherSession$Status.REQUESTED,com.masterlearning.platform.modules.privateTeacher.entity.PrivateTeacherSession$Status.CONFIRMED) and s.startsAt < :endAt and s.endsAt > :startAt")
 List<PrivateTeacherSession> findByTeacherIdOrderByStartsAtDesc(UUID teacherId);
 List<PrivateTeacherSession> findByLearnerIdOrderByStartsAtDesc(UUID learnerId);

 long countTeacherOverlap(@Param("teacherId") UUID teacherId,@Param("startAt") Instant startAt,@Param("endAt") Instant endAt);
}