package com.masterlearning.platform.modules.academy;

import com.masterlearning.platform.modules.program.entity.ProgramStatus;
import com.masterlearning.platform.modules.program.repository.ProgramRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademyProgramOverviewService {

    private final ProgramRepository programs;

    public AcademyProgramOverviewService(ProgramRepository programs) {
        this.programs = programs;
    }

    @Transactional(readOnly = true)
    public AcademyProgramOverviewResponse getOverview() {
        return new AcademyProgramOverviewResponse(
                programs.count(),
                programs.countByStatus(ProgramStatus.DRAFT),
                programs.countByStatus(ProgramStatus.PUBLISHED),
                programs.countByStatus(ProgramStatus.ACTIVE),
                programs.countByStatus(ProgramStatus.PAUSED),
                programs.countByStatus(ProgramStatus.COMPLETED),
                programs.countByStatus(ProgramStatus.ARCHIVED));
    }
}
