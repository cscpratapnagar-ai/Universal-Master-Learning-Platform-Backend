package com.masterlearning.platform.modules.privateTeacher.repository;

import com.masterlearning.platform.modules.privateTeacher.entity.PrivateTeacherAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PrivateTeacherAvailabilityRepository extends JpaRepository<PrivateTeacherAvailability, UUID> {
    List<PrivateTeacherAvailability> findByTeacherIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(UUID teacherId);
    long countByActiveTrue();
}
