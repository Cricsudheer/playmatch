package com.example.playmatch.auth.security;

import com.example.playmatch.auth.exception.AccountLockedException;
import com.example.playmatch.auth.model.User;
import com.example.playmatch.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Check if account is locked
        if (user.getLockoutUntil() != null && user.getLockoutUntil().isAfter(OffsetDateTime.now())) {
            throw new AccountLockedException("Account is temporarily locked");
        }

        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPasswordHash(),
            !user.isDeleted(), // enabled
            true, // accountNonExpired
            true, // credentialsNonExpired
            user.getLockoutUntil() == null || user.getLockoutUntil().isBefore(OffsetDateTime.now()), // accountNonLocked
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
