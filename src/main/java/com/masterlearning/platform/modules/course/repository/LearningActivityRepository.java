package com.masterlearning.platform.modules.course.repository;

import com.masterlearning.platform.modules.course.entity.LearningActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LearningActivityRepository extends JpaRepository<LearningActivity, UUID> {
    List<LearningActivity> findTop20ByEnrollmentIdOrderByOccurredAtDesc(UUID enrollmentId);

    @Query("select coalesce(sum(a.durationSeconds), 0) from LearningActivity a where a.enrollment.id = :enrollmentId")
    long totalDurationSeconds(UUID enrollmentId);

    @Query("select count(distinct a.lesson.id) from LearningActivity a where a.enrollment.id = :enrollmentId")
    long activeLessonCount(UUID enrollmentId);

    @Query("select max(a.occurredAt) from LearningActivity a where a.enrollment.id = :enrollmentId")
    Instant lastActivityAt(UUID enrollmentId);
}
