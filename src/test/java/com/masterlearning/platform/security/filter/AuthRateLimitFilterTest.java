package com.masterlearning.platform.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthRateLimitFilterTest {
    @Test
    void limitsLoginRequestsPerClientWithinOneMinute() throws Exception {
        AuthRateLimitFilter filter = new AuthRateLimitFilter(new ObjectMapper());
        AtomicInteger allowed = new AtomicInteger();

        for (int i = 0; i < 6; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            request.setRemoteAddr("192.0.2.10");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, (req, res) -> allowed.incrementAndGet());

            if (i < 5) {
                assertEquals(200, response.getStatus());
            } else {
                assertEquals(429, response.getStatus());
                assertEquals("60", response.getHeader("Retry-After"));
            }
        }

        assertEquals(5, allowed.get());
    }

    @Test
    void doesNotLimitUnrelatedEndpoints() throws Exception {
        AuthRateLimitFilter filter = new AuthRateLimitFilter(new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/courses");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicInteger allowed = new AtomicInteger();

        filter.doFilter(request, response, (req, res) -> allowed.incrementAndGet());

        assertEquals(1, allowed.get());
        assertEquals(200, response.getStatus());
    }
}
