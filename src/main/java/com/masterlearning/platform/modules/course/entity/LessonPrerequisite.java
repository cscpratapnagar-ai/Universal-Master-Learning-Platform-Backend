package com.masterlearning.platform.modules.course.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "lesson_prerequisites")
public class LessonPrerequisite {

    @EmbeddedId
    private Id id;

    protected LessonPrerequisite() {}

    public LessonPrerequisite(UUID lessonId, UUID prerequisiteLessonId) {
        this.id = new Id(lessonId, prerequisiteLessonId);
    }

    public UUID getLessonId() { return id.lessonId; }
    public UUID getPrerequisiteLessonId() { return id.prerequisiteLessonId; }

    @Embeddable
    public static class Id implements Serializable {
        @Column(name = "lesson_id")
        private UUID lessonId;

        @Column(name = "prerequisite_lesson_id")
        private UUID prerequisiteLessonId;

        protected Id() {}

        public Id(UUID lessonId, UUID prerequisiteLessonId) {
            this.lessonId = lessonId;
            this.prerequisiteLessonId = prerequisiteLessonId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Id other)) return false;
            return Objects.equals(lessonId, other.lessonId)
                    && Objects.equals(prerequisiteLessonId, other.prerequisiteLessonId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lessonId, prerequisiteLessonId);
        }
    }
}