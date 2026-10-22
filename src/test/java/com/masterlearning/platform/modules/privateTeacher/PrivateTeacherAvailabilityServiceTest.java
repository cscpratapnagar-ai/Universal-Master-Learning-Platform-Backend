package com.masterlearning.platform.modules.privateTeacher;

import com.masterlearning.platform.common.exception.ConflictException;
import com.masterlearning.platform.modules.privateTeacher.entity.PrivateTeacherAvailability;
import com.masterlearning.platform.modules.privateTeacher.repository.PrivateTeacherAvailabilityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrivateTeacherAvailabilityServiceTest {

    @Mock
    private PrivateTeacherAvailabilityRepository repository;

    @InjectMocks
    private PrivateTeacherAvailabilityService service;

    @Test
    void rejectsAvailabilityWhoseEndIsNotAfterStart() {
        var teacherId = UUID.randomUUID();
        var request = new PrivateTeacherAvailabilityRequest(
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(10, 0), "UTC");

        var error = assertThrows(IllegalArgumentException.class,
                () -> service.create(teacherId, request));

        assertEquals("endTime must be after startTime", error.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsInvalidTimezoneBeforeSaving() {
        var teacherId = UUID.randomUUID();
        var request = new PrivateTeacherAvailabilityRequest(
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0), "Not/A_Real_Zone");

        var error = assertThrows(IllegalArgumentException.class,
                () -> service.create(teacherId, request));

        assertEquals("Invalid availability timezone", error.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void mapsDatabaseExclusionViolationToConflict() {
        var teacherId = UUID.randomUUID();
        var request = new PrivateTeacherAvailabilityRequest(
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0), "UTC");
        var databaseError = new DataIntegrityViolationException(
                "availability constraint failed", new SQLException("overlap", "23P01"));
        when(repository.save(any(PrivateTeacherAvailability.class))).thenThrow(databaseError);

        assertThrows(ConflictException.class, () -> service.create(teacherId, request));
    }

    @Test
    void rethrowsUnrelatedDatabaseIntegrityViolation() {
        var teacherId = UUID.randomUUID();
        var request = new PrivateTeacherAvailabilityRequest(
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0), "UTC");
        var databaseError = new DataIntegrityViolationException(
                "unrelated integrity failure", new SQLException("foreign key violation", "23503"));
        when(repository.save(any(PrivateTeacherAvailability.class))).thenThrow(databaseError);

        var error = assertThrows(DataIntegrityViolationException.class,
                () -> service.create(teacherId, request));

        assertSame(databaseError, error);
    }
}
