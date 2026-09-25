package com.masterlearning.platform.modules.program.repository;

import com.masterlearning.platform.modules.program.entity.ProgramDependency;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ProgramDependencyRepository extends JpaRepository<ProgramDependency,UUID> {
 @EntityGraph(attributePaths={"program","program.organization","predecessor","successor"})
 List<ProgramDependency> findByProgramIdOrderByPredecessorSortOrderAscSuccessorSortOrderAsc(UUID programId);
 @EntityGraph(attributePaths={"program","program.organization","predecessor","successor"})
 Optional<ProgramDependency> findWithProgramAndOrganizationById(UUID id);
 boolean existsByPredecessorIdAndSuccessorId(UUID predecessorId, UUID successorId);
}
