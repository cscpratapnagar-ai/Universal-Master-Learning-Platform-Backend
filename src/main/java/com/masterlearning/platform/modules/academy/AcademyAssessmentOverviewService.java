package com.masterlearning.platform.modules.academy;

import com.masterlearning.platform.modules.assessment.repository.AssessmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademyAssessmentOverviewService {

    private final AssessmentRepository assessments;
    private final AcademyScopeService scope;

    public AcademyAssessmentOverviewService(AssessmentRepository assessments, AcademyScopeService scope) {
        this.assessments = assessments;
        this.scope = scope;
    }

    @Transactional(readOnly = true)
    public AcademyAssessmentOverviewResponse getOverview() {
        var list = scope.isGlobal() ? assessments.findAll() : scope.accessibleOrganizationIds().stream().flatMap(id -> assessments.findByOrganizationId(id).stream()).toList();
        long total = list.size();
        long withCourse = list.stream().map(a -> a.getCourse()).filter(java.util.Objects::nonNull).count();
        long averageAttempts = total == 0 ? 0 : Math.round(list.stream().mapToInt(a -> a.getMaxAttempts()).average().orElse(0));
        return new AcademyAssessmentOverviewResponse(total, withCourse, averageAttempts);
    }
}
