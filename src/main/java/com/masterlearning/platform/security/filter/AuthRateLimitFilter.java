package com.masterlearning.platform.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterlearning.platform.common.api.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {
    private static final long WINDOW_MILLIS = 60_000L;
    private static final int MAX_CLIENT_BUCKETS = 10_000;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public AuthRateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getMethod().equals("POST") || limitFor(request.getRequestURI()) == 0;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        int limit = limitFor(request.getRequestURI());
        String key = request.getRemoteAddr() + ":" + request.getRequestURI();
        long now = System.currentTimeMillis();
        Window window = windows.compute(key, (ignored, current) -> {
            if (current == null || now - current.startedAt >= WINDOW_MILLIS) {
                return new Window(now, new AtomicInteger(1));
            }
            current.count.incrementAndGet();
            return current;
        });

        if (windows.size() > MAX_CLIENT_BUCKETS) {
            windows.entrySet().removeIf(entry -> now - entry.getValue().startedAt >= WINDOW_MILLIS);
        }

        if (window.count.get() > limit) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Retry-After", "60");
            objectMapper.writeValue(response.getOutputStream(),
                    ApiErrorResponse.of("RATE_LIMITED",
                            "Too many authentication requests. Please try again shortly.", Map.of()));
            return;
        }
        chain.doFilter(request, response);
    }

    private int limitFor(String path) {
        return switch (path) {
            case "/api/v1/auth/login", "/api/v1/auth/forgot-password",
                    "/api/v1/auth/reset-password" -> 5;
            case "/api/v1/auth/register" -> 10;
            case "/api/v1/auth/refresh" -> 30;
            default -> 0;
        };
    }

    private record Window(long startedAt, AtomicInteger count) {}
}
