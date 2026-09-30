package com.masterlearning.platform.modules.privateTeacher;

import com.masterlearning.platform.modules.privateTeacher.entity.PrivateTeacherAvailability;
import com.masterlearning.platform.modules.privateTeacher.repository.PrivateTeacherAvailabilityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class PrivateTeacherAvailabilityService {
    private final PrivateTeacherAvailabilityRepository repository;

    public PrivateTeacherAvailabilityService(PrivateTeacherAvailabilityRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PrivateTeacherAvailabilityResponse> get(UUID teacherId) {
        return repository.findByTeacherIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(teacherId)
                .stream().map(PrivateTeacherAvailabilityResponse::from).toList();
    }

    @Transactional
    public PrivateTeacherAvailabilityResponse create(UUID teacherId, PrivateTeacherAvailabilityRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
        return PrivateTeacherAvailabilityResponse.from(repository.save(
                new PrivateTeacherAvailability(teacherId, request.dayOfWeek(), request.startTime(), request.endTime(), request.timezone())));
    }

    @Transactional
    public void disable(UUID teacherId, UUID availabilityId) {
        PrivateTeacherAvailability item = repository.findById(availabilityId)
                .orElseThrow(() -> new IllegalArgumentException("Availability not found"));
        if (!item.getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("Availability does not belong to teacher");
        }
        item.disable();
    }
}
