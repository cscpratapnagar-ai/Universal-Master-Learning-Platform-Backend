package com.masterlearning.platform.modules.program.repository;

import com.masterlearning.platform.modules.program.entity.Program;
import com.masterlearning.platform.modules.program.entity.ProgramStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.*;

public interface ProgramRepository extends JpaRepository<Program,UUID>{
    Optional<Program> findBySlug(String slug);
    @EntityGraph(attributePaths="organization")
    Optional<Program> findWithOrganizationById(UUID id);
    @EntityGraph(attributePaths="organization")
    List<Program> findByOrganizationIdOrderByTitleAsc(UUID organizationId);
    long countByStatus(ProgramStatus status);
}
