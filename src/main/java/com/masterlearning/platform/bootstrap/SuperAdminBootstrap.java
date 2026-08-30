package com.masterlearning.platform.bootstrap;

import com.masterlearning.platform.modules.identity.repository.RoleRepository;
import com.masterlearning.platform.modules.user.entity.User;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class SuperAdminBootstrap {

    private static final String EMAIL = "superadmin@masterlearning.local";
    private static final String PASSWORD = "SuperAdmin@123";

    @Bean
    ApplicationRunner superAdminBootstrapRunner(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> bootstrap(userRepository, roleRepository, passwordEncoder);
    }

    @Transactional
    void bootstrap(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        User user = userRepository.findByEmailIgnoreCase(EMAIL)
                .orElseGet(() -> new User(
                        EMAIL,
                        passwordEncoder.encode(PASSWORD),
                        "Super",
                        "Admin"
                ));

        if (!passwordEncoder.matches(PASSWORD, user.getPasswordHash())) {
            user.updatePasswordHash(passwordEncoder.encode(PASSWORD));
        }
        user.enable();

        roleRepository.findByCode("SUPER_ADMIN")
                .ifPresent(user::assignRole);

        userRepository.save(user);
    }
}
