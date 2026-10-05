package com.masterlearning.platform.modules.academy;

import com.masterlearning.platform.modules.course.entity.CourseStatus;
import com.masterlearning.platform.modules.course.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademyCourseOverviewService {

    private final CourseRepository courses;
    private final AcademyScopeService scope;

    public AcademyCourseOverviewService(CourseRepository courses) {
        this.courses = courses;
    }

    @Transactional(readOnly = true)
    public AcademyCourseOverviewResponse getOverview() {
        if (scope.isGlobal()) return new AcademyCourseOverviewResponse(courses.count(), courses.countByStatus(CourseStatus.PUBLISHED), courses.countByStatus(CourseStatus.DRAFT), courses.countByStatus(CourseStatus.ARCHIVED));
        long total=0,published=0,draft=0,archived=0;
        for (var id: scope.accessibleOrganizationIds()) { total+=courses.countByOrganizationId(id); published+=courses.countByOrganizationIdAndStatus(id,CourseStatus.PUBLISHED); draft+=courses.countByOrganizationIdAndStatus(id,CourseStatus.DRAFT); archived+=courses.countByOrganizationIdAndStatus(id,CourseStatus.ARCHIVED); }
        return new AcademyCourseOverviewResponse(total,published,draft,archived);
    }
}
