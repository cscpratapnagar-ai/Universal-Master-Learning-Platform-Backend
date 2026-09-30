package com.masterlearning.platform.modules.academy;

import com.masterlearning.platform.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademyTeacherOverviewService {

    private final UserRepository users;

    public AcademyTeacherOverviewService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public AcademyTeacherOverviewResponse getOverview() {
        return new AcademyTeacherOverviewResponse(
                users.countEnabledByRoleCode("TEACHER"),
                users.countByRoleCode("TEACHER"),
                users.countEnabledByRoleCode("INSTRUCTOR"));
    }
}
