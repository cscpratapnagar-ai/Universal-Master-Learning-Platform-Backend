package com.masterlearning.platform.modules.privateTeacher;

import com.masterlearning.platform.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrivateTeacherOverviewService {

    private final UserRepository users;

    public PrivateTeacherOverviewService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public PrivateTeacherOverviewResponse getOverview() {
        long eligible = users.countEnabledByRoleCode("TEACHER") + users.countEnabledByRoleCode("INSTRUCTOR");
        return new PrivateTeacherOverviewResponse(eligible, 0);
    }
}
