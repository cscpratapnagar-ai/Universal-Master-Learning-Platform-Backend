package com.masterlearning.platform.modules.course.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "learning_activities")
public class LearningActivity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected LearningActivity() {}

    public LearningActivity(Enrollment enrollment, Lesson lesson, UUID userId,
                            String eventType, int durationSeconds) {
        this.enrollment = enrollment;
        this.lesson = lesson;
        this.userId = userId;
        this.eventType = eventType;
        this.durationSeconds = durationSeconds;
        this.occurredAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Enrollment getEnrollment() { return enrollment; }
    public Lesson getLesson() { return lesson; }
    public UUID getUserId() { return userId; }
    public String getEventType() { return eventType; }
    public int getDurationSeconds() { return durationSeconds; }
    public Instant getOccurredAt() { return occurredAt; }
}
