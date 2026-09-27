package com.masterlearning.platform.modules.program.repository;
import com.masterlearning.platform.modules.program.entity.ProgramActivity;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface ProgramActivityRepository extends JpaRepository<ProgramActivity,UUID> {
 @EntityGraph(attributePaths={"program","program.organization"})
 List<ProgramActivity> findTop100ByProgramIdOrderByCreatedAtDesc(UUID programId);\n @Query("select a from ProgramActivity a where a.program.id = :programId and a.actor = :actor order by a.createdAt desc")\n List<ProgramActivity> findByProgramIdAndActorOrderByCreatedAtDesc(@Param("programId") UUID programId,@Param("actor") String actor,org.springframework.data.domain.Pageable pageable);
}