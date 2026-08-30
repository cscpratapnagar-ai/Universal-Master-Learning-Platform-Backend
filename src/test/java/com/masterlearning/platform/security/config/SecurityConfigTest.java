package com.masterlearning.platform.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterlearning.platform.security.filter.JwtAuthenticationFilter;
import com.masterlearning.platform.modules.user.mapper.UserMapper;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThatCode;

class SecurityConfigTest {

    @Test
    void securityFilterChain_buildsWithProtectedByDefaultPolicy() {
        SecurityConfig config = new SecurityConfig();
        HttpSecurity http = new HttpSecurity(
                Mockito.mock(org.springframework.security.config.annotation.web.builders.WebSecurity.class),
                new org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder(Mockito.mock(org.springframework.security.config.annotation.ObjectPostProcessor.class)),
                java.util.Map.of()
        );

        assertThatCode(() -> {
            SecurityFilterChain ignored = config.securityFilterChain(
                    http,
                    Mockito.mock(JwtAuthenticationFilter.class),
                    new ObjectMapper(),
                    Mockito.mock(UserRepository.class),
                    Mockito.mock(UserMapper.class)
            );
        }).doesNotThrowAnyException();
    }
}
