package com.masterlearning.platform.modules.program.repository;
import com.masterlearning.platform.modules.program.entity.ProgramActivity;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface ProgramActivityRepository extends JpaRepository<ProgramActivity,UUID> {
 @EntityGraph(attributePaths={"program","program.organization"})
 List<ProgramActivity> findTop100ByProgramIdOrderByCreatedAtDesc(UUID programId);
}