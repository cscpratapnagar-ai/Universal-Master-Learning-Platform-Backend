package com.masterlearning.platform.modules.user.service;

import com.masterlearning.platform.common.exception.ForbiddenException;
import com.masterlearning.platform.modules.identity.entity.Role;
import com.masterlearning.platform.modules.identity.repository.RoleRepository;
import com.masterlearning.platform.modules.user.dto.request.UpdateUserStatusRequest;
import com.masterlearning.platform.modules.user.entity.User;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.modules.user.service.UserManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
@ActiveProfiles("local")
@EnabledIfEnvironmentVariable(named = "CI", matches = "true")
class UserManagementSuperAdminConcurrencyTest {

    @Autowired
    private UserManagementService userManagement;

    @Autowired
    private UserRepository users;

    @Autowired
    private RoleRepository roles;

    @Test
    void concurrentDisableRequestsCannotDisableBothLastSuperAdmins() throws Exception {
        // This test requires a dedicated integration-test database with no pre-existing
        // active Super Admin accounts. Never mutate existing privileged accounts.
        long baselineActiveSuperAdmins = users.countActiveSuperAdmins();
        assumeTrue(baselineActiveSuperAdmins == 0,
                "Run only against an isolated database with no pre-existing active Super Admin accounts");

        Role superAdmin = roles.findByCode("SUPER_ADMIN")
                .orElseThrow(() -> new AssertionError("SUPER_ADMIN role must exist in the integration-test database"));

        User firstAdmin = new User("concurrency-admin-one-" + UUID.randomUUID() + "@test.invalid",
                "test-password-hash", "Concurrency", "AdminOne");
        firstAdmin.assignRole(superAdmin);
        User secondAdmin = new User("concurrency-admin-two-" + UUID.randomUUID() + "@test.invalid",
                "test-password-hash", "Concurrency", "AdminTwo");
        secondAdmin.assignRole(superAdmin);

        firstAdmin = users.saveAndFlush(firstAdmin);
        secondAdmin = users.saveAndFlush(secondAdmin);
        UUID firstId = firstAdmin.getId();
        UUID secondId = secondAdmin.getId();

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Boolean> first = executor.submit(() ->
                    disableAfterBarrier(firstId, ready, start));
            Future<Boolean> second = executor.submit(() ->
                    disableAfterBarrier(secondId, ready, start));

            assertTrue(ready.await(10, TimeUnit.SECONDS),
                    "Concurrent disable workers did not become ready");
            start.countDown();

            boolean firstDisabled = first.get(30, TimeUnit.SECONDS);
            boolean secondDisabled = second.get(30, TimeUnit.SECONDS);

            assertEquals(1, (firstDisabled ? 1 : 0) + (secondDisabled ? 1 : 0),
                    "Exactly one concurrent disable should succeed; the other must be rejected");
            assertEquals(1, users.countActiveSuperAdmins(),
                    "Exactly one active Super Admin must remain");
        } finally {
            start.countDown();
            executor.shutdownNow();
            users.deleteAllById(java.util.List.of(firstId, secondId));
        }
    }

    private boolean disableAfterBarrier(UUID userId, CountDownLatch ready,
                                        CountDownLatch start) throws Exception {
        ready.countDown();
        if (!start.await(10, TimeUnit.SECONDS)) {
            throw new AssertionError("Concurrent disable start signal timed out");
        }

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("integration-actor@test.invalid", "test"));
        try {
            userManagement.updateStatus(userId, new UpdateUserStatusRequest(false));
            return true;
        } catch (ForbiddenException expected) {
            return false;
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
