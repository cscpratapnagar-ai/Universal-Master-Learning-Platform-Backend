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

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);
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
}