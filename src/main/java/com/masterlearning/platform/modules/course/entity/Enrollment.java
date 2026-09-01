package com.masterlearning.platform.modules.course.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import com.masterlearning.platform.modules.user.entity.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "course_enrollments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "user_id"})
)
public class Enrollment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int progressPercent = 0;

    private Instant completedAt;

    protected Enrollment() {}

    public Enrollment(Course course, User user) {
        this.course = course;
        this.user = user;
    }

    public UUID getId() {
        return id;
    }

    public Course getCourse() {
        return course;
    }

    public User getUser() {
        return user;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public boolean isCompleted() {
        return progressPercent >= 100;
    }

    public void updateProgress(int progressPercent) {
        this.progressPercent = Math.max(0, Math.min(100, progressPercent));

        if (this.progressPercent == 100 && completedAt == null) {
            completedAt = Instant.now();
        } else if (this.progressPercent < 100) {
            completedAt = null;
        }
    }
}
