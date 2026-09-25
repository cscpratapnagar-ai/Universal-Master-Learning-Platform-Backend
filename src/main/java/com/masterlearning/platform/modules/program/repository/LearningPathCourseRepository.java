package com.masterlearning.platform.modules.program.repository;
import com.masterlearning.platform.modules.program.entity.LearningPathCourse; import org.springframework.data.jpa.repository.EntityGraph; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface LearningPathCourseRepository extends JpaRepository<LearningPathCourse,UUID>{
 @EntityGraph(attributePaths={"learningPath","learningPath.program","learningPath.program.organization","course","course.organization"})
 List<LearningPathCourse> findByLearningPathProgramIdOrderByLearningPathTitleAscSortOrderAsc(UUID programId);
}