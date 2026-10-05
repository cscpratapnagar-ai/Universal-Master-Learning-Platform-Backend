package com.masterlearning.platform.modules.academy;

import com.masterlearning.platform.modules.program.entity.ProgramStatus;
import com.masterlearning.platform.modules.program.repository.ProgramRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademyProgramOverviewService {

    private final ProgramRepository programs;
    private final AcademyScopeService scope;

    public AcademyProgramOverviewService(ProgramRepository programs, AcademyScopeService scope) {
        this.programs = programs;
        this.scope = scope;
    }

    @Transactional(readOnly = true)
    public AcademyProgramOverviewResponse getOverview() {
        if (scope.isGlobal()) return new AcademyProgramOverviewResponse(programs.count(), programs.countByStatus(ProgramStatus.DRAFT), programs.countByStatus(ProgramStatus.PUBLISHED), programs.countByStatus(ProgramStatus.ACTIVE), programs.countByStatus(ProgramStatus.PAUSED), programs.countByStatus(ProgramStatus.COMPLETED), programs.countByStatus(ProgramStatus.ARCHIVED));
        long[] x=new long[7]; for(var id:scope.accessibleOrganizationIds()){x[0]+=programs.countByOrganizationId(id); x[1]+=programs.countByOrganizationIdAndStatus(id,ProgramStatus.DRAFT); x[2]+=programs.countByOrganizationIdAndStatus(id,ProgramStatus.PUBLISHED); x[3]+=programs.countByOrganizationIdAndStatus(id,ProgramStatus.ACTIVE); x[4]+=programs.countByOrganizationIdAndStatus(id,ProgramStatus.PAUSED); x[5]+=programs.countByOrganizationIdAndStatus(id,ProgramStatus.COMPLETED); x[6]+=programs.countByOrganizationIdAndStatus(id,ProgramStatus.ARCHIVED);} return new AcademyProgramOverviewResponse(x[0],x[1],x[2],x[3],x[4],x[5],x[6]);
    }
}
