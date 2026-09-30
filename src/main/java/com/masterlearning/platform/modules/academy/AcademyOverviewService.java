package com.masterlearning.platform.modules.academy;

import com.masterlearning.platform.modules.course.entity.CourseStatus;
import com.masterlearning.platform.modules.course.repository.CourseRepository;
import com.masterlearning.platform.modules.program.entity.ProgramStatus;
import com.masterlearning.platform.modules.program.repository.ProgramRepository;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademyOverviewService {
    private final UserRepository users;
    private final CourseRepository courses;
    private final ProgramRepository programs;

    public AcademyOverviewService(
            UserRepository users,
            CourseRepository courses,
            ProgramRepository programs) {
        this.users = users;
        this.courses = courses;
        this.programs = programs;
    }

    @Transactional(readOnly = true)
    public AcademyOverviewResponse overview() {
        return new AcademyOverviewResponse(
                users.countByEnabledTrue(),
                courses.countByStatus(CourseStatus.PUBLISHED),
                programs.countByStatus(ProgramStatus.ACTIVE),
                programs.countByStatus(ProgramStatus.PUBLISHED),
                programs.countByStatus(ProgramStatus.PAUSED),
                programs.countByStatus(ProgramStatus.COMPLETED)
        );
    }
}
