package com.masterlearning.platform.modules.privateTeacher.repository;
import com.masterlearning.platform.modules.privateTeacher.entity.PrivateTeacherProfile;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional; import java.util.UUID;
public interface PrivateTeacherProfileRepository extends JpaRepository<PrivateTeacherProfile,UUID>{ Optional<PrivateTeacherProfile> findByTeacherId(UUID teacherId); }