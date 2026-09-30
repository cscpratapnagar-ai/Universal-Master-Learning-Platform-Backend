package com.masterlearning.platform.modules.academy;

import com.masterlearning.platform.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademyLearnerOverviewService {

    private final UserRepository users;

    public AcademyLearnerOverviewService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public AcademyLearnerOverviewResponse getOverview() {
        return new AcademyLearnerOverviewResponse(
                users.countEnabledByRoleCode("LEARNER"),
                users.countByRoleCode("LEARNER"),
                users.countEnabledByRoleCode("STUDENT"));
    }
}
