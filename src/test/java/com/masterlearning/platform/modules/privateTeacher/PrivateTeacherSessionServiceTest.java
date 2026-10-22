package com.masterlearning.platform.modules.privateTeacher;

import com.masterlearning.platform.modules.privateTeacher.entity.PrivateTeacherAvailability;
import com.masterlearning.platform.modules.privateTeacher.entity.PrivateTeacherSession;
import com.masterlearning.platform.modules.privateTeacher.repository.PrivateTeacherAvailabilityRepository;
import com.masterlearning.platform.modules.privateTeacher.repository.PrivateTeacherSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrivateTeacherSessionServiceTest {

    @Mock
    private PrivateTeacherSessionRepository sessions;

    @Mock
    private PrivateTeacherAvailabilityRepository availability;

    @InjectMocks
    private PrivateTeacherSessionService service;

    private UUID teacherId;
    private UUID learnerId;

    @BeforeEach
    void setUp() {
        teacherId = UUID.randomUUID();
        learnerId = UUID.randomUUID();
    }

    @Test
    void rejectsSelfBooking() {
        var start = Instant.now().plus(Duration.ofDays(2));
        var request = request(teacherId, start, start.plus(Duration.ofHours(1)));

        assertThrows(IllegalArgumentException.class, () -> service.request(teacherId, request));
        verifyNoInteractions(sessions, availability);
    }

    @Test
    void rejectsSessionWhoseEndIsNotAfterStart() {
        var start = Instant.now().plus(Duration.ofDays(2));
        var request = request(teacherId, start, start);

        assertThrows(IllegalArgumentException.class, () -> service.request(learnerId, request));
        verifyNoInteractions(sessions, availability);
    }

    @Test
    void rejectsSessionInThePast() {
        var start = Instant.now().minus(Duration.ofHours(2));
        var request = request(teacherId, start, start.plus(Duration.ofMinutes(30)));

        assertThrows(IllegalArgumentException.class, () -> service.request(learnerId, request));
        verifyNoInteractions(sessions, availability);
    }

    @Test
    void rejectsOverlappingTeacherSessionBeforeCheckingAvailability() {
        var start = Instant.now().plus(Duration.ofDays(2));
        var request = request(teacherId, start, start.plus(Duration.ofMinutes(60)));
        when(sessions.countTeacherOverlap(teacherId, request.startsAt(), request.endsAt())).thenReturn(1L);

        var error = assertThrows(IllegalArgumentException.class, () -> service.request(learnerId, request));

        assertEquals("Teacher already has an overlapping session", error.getMessage());
        verifyNoInteractions(availability);
        verify(sessions, never()).save(any());
    }

    @Test
    void rejectsRequestOutsideActiveTeacherAvailability() {
        var date = LocalDate.now(ZoneOffset.UTC).plusDays(5);
        var start = date.atTime(12, 0).toInstant(ZoneOffset.UTC);
        var request = request(teacherId, start, start.plus(Duration.ofMinutes(60)));
        when(sessions.countTeacherOverlap(teacherId, request.startsAt(), request.endsAt())).thenReturn(0L);
        when(availability.findByTeacherIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(teacherId))
                .thenReturn(List.of(new PrivateTeacherAvailability(
                        teacherId, date.getDayOfWeek(), LocalTime.of(9, 0), LocalTime.of(10, 0), "UTC")));

        var error = assertThrows(IllegalArgumentException.class, () -> service.request(learnerId, request));

        assertEquals("Requested session is outside the teacher's availability", error.getMessage());
        verify(sessions, never()).save(any());
    }

    @Test
    void rejectsConfirmationBySomeoneOtherThanAssignedTeacher() {
        var start = Instant.now().plus(Duration.ofDays(2));
        var session = new PrivateTeacherSession(teacherId, learnerId, start, start.plus(Duration.ofHours(1)), "UTC", "Maths", null);
        var sessionId = UUID.randomUUID();
        when(sessions.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(SecurityException.class, () -> service.confirm(learnerId, sessionId));
        assertEquals(PrivateTeacherSession.Status.REQUESTED, session.getStatus());
    }

    @Test
    void rejectsCompletionBeforeSessionIsConfirmed() {
        var start = Instant.now().plus(Duration.ofDays(2));
        var session = new PrivateTeacherSession(teacherId, learnerId, start, start.plus(Duration.ofHours(1)), "UTC", "Maths", null);
        var sessionId = UUID.randomUUID();
        when(sessions.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(IllegalStateException.class, () -> service.complete(teacherId, sessionId));
        assertEquals(PrivateTeacherSession.Status.REQUESTED, session.getStatus());
    }

    private PrivateTeacherSessionRequest request(UUID requestedTeacher, Instant start, Instant end) {
        return new PrivateTeacherSessionRequest(requestedTeacher, start, end, "UTC", "Maths", null);
    }
}
