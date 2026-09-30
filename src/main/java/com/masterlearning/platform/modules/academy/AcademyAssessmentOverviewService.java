package com.masterlearning.platform.modules.academy;

import com.masterlearning.platform.modules.assessment.repository.AssessmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademyAssessmentOverviewService {

    private final AssessmentRepository assessments;

    public AcademyAssessmentOverviewService(AssessmentRepository assessments) {
        this.assessments = assessments;
    }

    @Transactional(readOnly = true)
    public AcademyAssessmentOverviewResponse getOverview() {
        long total = assessments.count();
        long withCourse = assessments.findAll().stream()
                .map(a -> a.getCourse())
                .filter(java.util.Objects::nonNull)
                .count();
        long averageAttempts = total == 0 ? 0 :
                Math.round(assessments.findAll().stream()
                        .mapToInt(a -> a.getMaxAttempts())
                        .average().orElse(0));
        return new AcademyAssessmentOverviewResponse(total, withCourse, averageAttempts);
    }
}
