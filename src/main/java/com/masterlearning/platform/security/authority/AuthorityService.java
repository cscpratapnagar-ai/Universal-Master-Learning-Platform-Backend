package com.masterlearning.platform.security.authority;

import com.masterlearning.platform.modules.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class AuthorityService {

    public Set<GrantedAuthority> resolve(User user) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();

        user.getRoles().forEach(role -> {
            authorities.add(() -> "ROLE_" + role.getCode());

            role.getPermissions().forEach(permission ->
                    authorities.add(() -> permission.getCode())
            );
        });

        return Set.copyOf(authorities);
    }
}