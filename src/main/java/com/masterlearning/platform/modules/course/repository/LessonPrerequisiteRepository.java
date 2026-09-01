package com.masterlearning.platform.modules.course.repository;

import com.masterlearning.platform.modules.course.entity.LessonPrerequisite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LessonPrerequisiteRepository extends JpaRepository<LessonPrerequisite, LessonPrerequisite.Id> {
    List<LessonPrerequisite> findByIdLessonId(UUID lessonId);
    void deleteByIdLessonIdAndIdPrerequisiteLessonId(UUID lessonId, UUID prerequisiteLessonId);
    boolean existsByIdLessonIdAndIdPrerequisiteLessonId(UUID lessonId, UUID prerequisiteLessonId);
}