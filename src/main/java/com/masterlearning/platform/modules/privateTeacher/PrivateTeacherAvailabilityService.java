package com.masterlearning.platform.modules.privateTeacher;

import com.masterlearning.platform.common.exception.ConflictException;
import com.masterlearning.platform.modules.privateTeacher.entity.PrivateTeacherAvailability;
import com.masterlearning.platform.modules.privateTeacher.repository.PrivateTeacherAvailabilityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.SQLException;
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
        String timezone = request.timezone().trim();
        try {
            return PrivateTeacherAvailabilityResponse.from(repository.save(
                    new PrivateTeacherAvailability(teacherId, request.dayOfWeek(), request.startTime(), request.endTime(), timezone)));
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            if (isAvailabilityOverlap(ex)) {
                throw new ConflictException("Availability overlaps an existing active window");
            }
            throw ex;
        }
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
    private boolean isAvailabilityOverlap(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof SQLException sqlException
                    && "23P01".equals(sqlException.getSQLState())) {
                return true;
            }
            String message = current.getMessage();
            if (message != null && message.contains("trg_private_teacher_availability_no_overlap")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
