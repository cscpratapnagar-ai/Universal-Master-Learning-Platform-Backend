package com.masterlearning.platform.security.authority;

import com.masterlearning.platform.modules.user.entity.User;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorityServiceTests {

    @Test
    void shouldReturnEmptyAuthoritiesForUserWithoutRoles() {
        User user = new User(
                "student@example.com",
                "hash",
                "Student",
                null
        );

        Set<String> authorities = new AuthorityService()
                .resolve(user)
                .stream()
                .map(authority -> authority.getAuthority())
                .collect(java.util.stream.Collectors.toSet());

        assertTrue(authorities.isEmpty());
    }
}