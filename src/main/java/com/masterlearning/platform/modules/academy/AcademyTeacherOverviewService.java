package com.masterlearning.platform.modules.academy;

import com.masterlearning.platform.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademyTeacherOverviewService {

    private final UserRepository users;
    private final AcademyScopeService scope;

    public AcademyTeacherOverviewService(UserRepository users, AcademyScopeService scope) {
        this.users = users;
        this.scope = scope;
    }

    @Transactional(readOnly = true)
    public AcademyTeacherOverviewResponse getOverview() {
        if (scope.isGlobal()) return new AcademyTeacherOverviewResponse(users.countEnabledByRoleCode("TEACHER"), users.countByRoleCode("TEACHER"), users.countEnabledByRoleCode("INSTRUCTOR"));
        long enabledTeacher=0, teacher=0, enabledInstructor=0; for(var id:scope.accessibleOrganizationIds()){enabledTeacher+=users.countActiveByOrganizationIdAndRoleCode(id,"TEACHER"); teacher+=users.countActiveByOrganizationIdAndRoleCode(id,"TEACHER"); enabledInstructor+=users.countActiveByOrganizationIdAndRoleCode(id,"INSTRUCTOR");} return new AcademyTeacherOverviewResponse(enabledTeacher,teacher,enabledInstructor);
    }
}
