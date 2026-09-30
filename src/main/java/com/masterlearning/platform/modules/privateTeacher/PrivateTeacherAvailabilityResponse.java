package com.masterlearning.platform.modules.privateTeacher;

import com.masterlearning.platform.modules.privateTeacher.entity.PrivateTeacherAvailability;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record PrivateTeacherAvailabilityResponse(
        UUID id, UUID teacherId, DayOfWeek dayOfWeek,
        LocalTime startTime, LocalTime endTime, String timezone, boolean active) {
    public static PrivateTeacherAvailabilityResponse from(PrivateTeacherAvailability a) {
        return new PrivateTeacherAvailabilityResponse(a.getId(), a.getTeacherId(), a.getDayOfWeek(),
                a.getStartTime(), a.getEndTime(), a.getTimezone(), a.isActive());
    }
}
