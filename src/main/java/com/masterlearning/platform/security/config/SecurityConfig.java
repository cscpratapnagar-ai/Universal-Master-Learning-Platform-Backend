package com.masterlearning.platform.security.config;

import com.masterlearning.platform.security.filter.JwtAuthenticationFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration @EnableWebSecurity @EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {
 @Bean public PasswordEncoder passwordEncoder(SecurityProperties p){return new BCryptPasswordEncoder(p.bcryptStrength());}
 @Bean public SecurityFilterChain securityFilterChain(HttpSecurity h,JwtAuthenticationFilter f)throws Exception{return h.csrf(c->c.disable()).cors(Customizer.withDefaults()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(a->a.requestMatchers("/api/v1/auth/**","/api/v1/health","/actuator/health","/actuator/info","/error").permitAll().anyRequest().authenticated()).addFilterBefore(f,UsernamePasswordAuthenticationFilter.class).build();}
}