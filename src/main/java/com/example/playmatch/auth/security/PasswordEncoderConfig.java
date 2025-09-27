package com.example.playmatch.auth.security;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(
            Argon2Factory.create(
                Argon2Factory.Argon2Types.ARGON2id,
                32,  // Salt length
                64   // Hash length
            )
        );
    }
}

class Argon2PasswordEncoder implements PasswordEncoder {
    private final Argon2 argon2;

    // Recommended values for password hashing
    private static final int ITERATIONS = 2;
    private static final int MEMORY = 65536;    // 64MB
    private static final int PARALLELISM = 1;

    public Argon2PasswordEncoder(Argon2 argon2) {
        this.argon2 = argon2;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        try {
            byte[] passwordBytes = rawPassword.toString().getBytes();
            return argon2.hash(ITERATIONS, MEMORY, PARALLELISM, passwordBytes);
        } finally {
            // Clear sensitive data
            if (rawPassword instanceof StringBuilder) {
                ((StringBuilder) rawPassword).setLength(0);
            }
        }
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        try {
            return argon2.verify(encodedPassword, rawPassword.toString().getBytes());
        } finally {
            // Clear sensitive data
            if (rawPassword instanceof StringBuilder) {
                ((StringBuilder) rawPassword).setLength(0);
            }
        }
    }
}
