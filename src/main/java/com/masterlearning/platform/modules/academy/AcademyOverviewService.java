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
    private final AcademyScopeService scope;

    public AcademyOverviewService(
            UserRepository users,
            CourseRepository courses,
            ProgramRepository programs,
            AcademyScopeService scope) {
        this.users = users;
        this.courses = courses;
        this.programs = programs;
        this.scope = scope;
    }

    @Transactional(readOnly = true)
    public AcademyOverviewResponse overview() {
        if (scope.isGlobal()) return new AcademyOverviewResponse(users.countByEnabledTrue(), courses.countByStatus(CourseStatus.PUBLISHED), programs.countByStatus(ProgramStatus.ACTIVE), programs.countByStatus(ProgramStatus.PUBLISHED), programs.countByStatus(ProgramStatus.PAUSED), programs.countByStatus(ProgramStatus.COMPLETED));
        long learners=0,publishedCourses=0,active=0,published=0,paused=0,completed=0; for(var id:scope.accessibleOrganizationIds()){learners+=users.countActiveByOrganizationIdAndRoleCode(id,"LEARNER"); publishedCourses+=courses.countByOrganizationIdAndStatus(id,CourseStatus.PUBLISHED); active+=programs.countByOrganizationIdAndStatus(id,ProgramStatus.ACTIVE); published+=programs.countByOrganizationIdAndStatus(id,ProgramStatus.PUBLISHED); paused+=programs.countByOrganizationIdAndStatus(id,ProgramStatus.PAUSED); completed+=programs.countByOrganizationIdAndStatus(id,ProgramStatus.COMPLETED);} return new AcademyOverviewResponse(learners,publishedCourses,active,published,paused,completed);
    }
}
