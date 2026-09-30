package com.masterlearning.platform.modules.academy;

import com.masterlearning.platform.modules.course.entity.CourseStatus;
import com.masterlearning.platform.modules.course.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademyCourseOverviewService {

    private final CourseRepository courses;

    public AcademyCourseOverviewService(CourseRepository courses) {
        this.courses = courses;
    }

    @Transactional(readOnly = true)
    public AcademyCourseOverviewResponse getOverview() {
        return new AcademyCourseOverviewResponse(
                courses.count(),
                courses.countByStatus(CourseStatus.PUBLISHED),
                courses.countByStatus(CourseStatus.DRAFT),
                courses.countByStatus(CourseStatus.ARCHIVED));
    }
}
