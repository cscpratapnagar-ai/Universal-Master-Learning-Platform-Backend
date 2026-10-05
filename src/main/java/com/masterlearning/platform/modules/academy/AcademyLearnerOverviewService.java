package com.masterlearning.platform.modules.academy;

import com.masterlearning.platform.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademyLearnerOverviewService {

    private final UserRepository users;
    private final AcademyScopeService scope;

    public AcademyLearnerOverviewService(UserRepository users, AcademyScopeService scope) {
        this.users = users;
        this.scope = scope;
    }

    @Transactional(readOnly = true)
    public AcademyLearnerOverviewResponse getOverview() {
        if (scope.isGlobal()) return new AcademyLearnerOverviewResponse(users.countEnabledByRoleCode("LEARNER"), users.countByRoleCode("LEARNER"), users.countEnabledByRoleCode("STUDENT"));
        long enabledLearner=0, learner=0, enabledStudent=0; for(var id:scope.accessibleOrganizationIds()){enabledLearner+=users.countActiveByOrganizationIdAndRoleCode(id,"LEARNER"); learner+=users.countActiveByOrganizationIdAndRoleCode(id,"LEARNER"); enabledStudent+=users.countActiveByOrganizationIdAndRoleCode(id,"STUDENT");} return new AcademyLearnerOverviewResponse(enabledLearner,learner,enabledStudent);
    }
}
