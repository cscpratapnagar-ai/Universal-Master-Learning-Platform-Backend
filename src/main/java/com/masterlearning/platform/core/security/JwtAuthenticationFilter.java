package com.masterlearning.platform.core.security;
import jakarta.servlet.*; import jakarta.servlet.http.*; import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; import org.springframework.security.core.authority.SimpleGrantedAuthority; import org.springframework.security.core.context.SecurityContextHolder; import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter; import java.io.IOException; import java.util.*;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
 private final JwtService jwt; private final UserRepository users;
 public JwtAuthenticationFilter(JwtService jwt,UserRepository users){this.jwt=jwt;this.users=users;}
 protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  String h=req.getHeader("Authorization");
  if(h!=null&&h.startsWith("Bearer ")){try{User u=users.findById(jwt.subject(h.substring(7))).orElse(null);if(u!=null&&u.getStatus()==UserStatus.ACTIVE){var auths=u.getRoles().stream().map(r->new SimpleGrantedAuthority("ROLE_"+r.getCode())).toList();SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(u.getId(),null,auths));}}catch(JwtException|IllegalArgumentException ignored){}}
  chain.doFilter(req,res);
 }
}