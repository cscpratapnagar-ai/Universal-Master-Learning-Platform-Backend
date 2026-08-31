package com.masterlearning.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class MasterLearningApplication {
    public static void main(String[] args) {
        SpringApplication.run(MasterLearningApplication.class, args);
    }

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of("system");
    }
}