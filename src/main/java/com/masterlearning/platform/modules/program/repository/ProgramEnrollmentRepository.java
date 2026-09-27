package com.masterlearning.platform.modules.program.repository;
import com.masterlearning.platform.modules.program.entity.ProgramEnrollment;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface ProgramEnrollmentRepository extends JpaRepository<ProgramEnrollment,UUID>{
 @EntityGraph(attributePaths={"program","program.organization","user"})
 List<ProgramEnrollment> findByProgramIdOrderByCreatedAtAsc(UUID programId);
 @EntityGraph(attributePaths={"program","program.organization"})
 List<ProgramEnrollment> findByUserIdOrderByCreatedAtDesc(UUID userId);
 @EntityGraph(attributePaths={"program","program.organization","user"})
 Optional<ProgramEnrollment> findByProgramIdAndUserId(UUID programId,UUID userId);
 long countByProgramIdAndStatus(UUID programId,String status);
 @Query("select e from ProgramEnrollment e join fetch e.user where e.program.id = :programId and e.status <> 'CANCELLED' order by e.progressPercent asc, e.createdAt asc")
 List<ProgramEnrollment> findActiveByProgramIdForIntervention(@Param("programId") UUID programId);
}