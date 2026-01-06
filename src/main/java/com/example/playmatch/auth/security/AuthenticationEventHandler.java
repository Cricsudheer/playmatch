package com.example.playmatch.auth.security;

import com.example.playmatch.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationEventHandler {

    private final UserRepository userRepository;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    @EventListener
    @Transactional
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String email = ((UserDetails) event.getAuthentication().getPrincipal()).getUsername();
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            user.setLastLoginAt(OffsetDateTime.now());
            user.setFailedLoginCount((short) 0);
            user.setLockoutUntil(null);
            userRepository.save(user);
            log.debug("Login success for user: {}", email);
        });
    }

    @EventListener
    @Transactional
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String email = event.getAuthentication().getName();
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            short newFailedCount = (short) (user.getFailedLoginCount() + 1);
            user.setFailedLoginCount(newFailedCount);

            if (newFailedCount >= MAX_FAILED_ATTEMPTS) {
                user.setLockoutUntil(OffsetDateTime.now().plusMinutes(LOCKOUT_MINUTES));
                log.warn("Account locked due to too many failed attempts: {}", email);
            }

            userRepository.save(user);
            log.debug("Login failure for user: {}. Failed attempts: {}", email, newFailedCount);
        });
    }
}
