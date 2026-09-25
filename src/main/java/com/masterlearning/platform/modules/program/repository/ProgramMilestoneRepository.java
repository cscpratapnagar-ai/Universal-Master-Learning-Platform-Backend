package com.masterlearning.platform.modules.program.repository;

import com.masterlearning.platform.modules.program.entity.ProgramMilestone;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ProgramMilestoneRepository extends JpaRepository<ProgramMilestone, UUID> {
    @EntityGraph(attributePaths = {"program", "program.organization"})
    List<ProgramMilestone> findByProgramIdOrderBySortOrderAscDueDateAsc(UUID programId);
    @EntityGraph(attributePaths = {"program", "program.organization"})
    Optional<ProgramMilestone> findWithProgramAndOrganizationById(UUID id);
}
