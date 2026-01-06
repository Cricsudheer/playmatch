package com.example.playmatch.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityBeansConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Configure Argon2 with recommended parameters
        // Memory: 64MB, Iterations: 2, Parallelism: 1, Salt length: 16, Hash length: 32
        return new Argon2PasswordEncoder(16, 32, 2, 64 * 1024, 1);
    }

    @Bean
    public java.time.Clock clock() {
        return java.time.Clock.systemUTC();
    }
}
