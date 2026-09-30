package com.masterlearning.platform.modules.privateTeacher.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "private_teacher_availability")
public class PrivateTeacherAvailability extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 12)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false, length = 80)
    private String timezone = "UTC";

    @Column(nullable = false)
    private boolean active = true;

    protected PrivateTeacherAvailability() {}

    public PrivateTeacherAvailability(UUID teacherId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, String timezone) {
        this.teacherId = teacherId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timezone = timezone;
    }

    public UUID getId() { return id; }
    public UUID getTeacherId() { return teacherId; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getTimezone() { return timezone; }
    public boolean isActive() { return active; }
    public void disable() { this.active = false; }
}
