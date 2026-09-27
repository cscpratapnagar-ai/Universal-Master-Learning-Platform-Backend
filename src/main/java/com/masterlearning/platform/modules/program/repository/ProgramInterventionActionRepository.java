package com.masterlearning.platform.modules.program.repository;

import com.masterlearning.platform.modules.program.entity.ProgramInterventionAction;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface ProgramInterventionActionRepository extends JpaRepository<ProgramInterventionAction,UUID> {
    @EntityGraph(attributePaths={"user"})
    List<ProgramInterventionAction> findByProgramIdAndUserIdOrderByCreatedAtDesc(UUID programId, UUID userId);
}