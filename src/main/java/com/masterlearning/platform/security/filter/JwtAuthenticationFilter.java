package com.masterlearning.platform.security.filter;

import com.masterlearning.platform.modules.user.entity.User;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.security.authority.AuthorityService;
import com.masterlearning.platform.security.authority.CurrentUserPrincipal;
import com.masterlearning.platform.security.jwt.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuthorityService authorityService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository,
            AuthorityService authorityService
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.authorityService = authorityService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveBearerToken(request.getHeader("Authorization"));

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            UUID userId = jwtService.extractUserId(token);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findWithAuthoritiesById(userId).orElse(null);

                if (user != null && user.isEnabled()) {
                    CurrentUserPrincipal principal =
                            new CurrentUserPrincipal(user.getId(), user.getEmail());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    authorityService.resolve(user)
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }
            }
        } catch (JwtException | IllegalArgumentException ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String resolveBearerToken(String header) {
        if (header == null) {
            return null;
        }

        String value = header.trim();

        if (!value.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return null;
        }

        String token = value.substring(BEARER_PREFIX.length()).trim();

        // Defensive compatibility: some API clients add the Bearer scheme automatically
        // while callers paste an already-prefixed value into the token field.
        if (token.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            token = token.substring(BEARER_PREFIX.length()).trim();
        }

        return token.isBlank() ? null : token;
    }
}
