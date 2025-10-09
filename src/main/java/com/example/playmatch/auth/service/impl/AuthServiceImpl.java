package com.example.playmatch.auth.service.impl;

import com.example.playmatch.api.model.*;
import com.example.playmatch.auth.exception.*;
import com.example.playmatch.auth.model.Gender;
import com.example.playmatch.auth.model.PasswordResetToken;
import com.example.playmatch.auth.model.User;
import com.example.playmatch.auth.repository.PasswordResetTokenRepository;
import com.example.playmatch.auth.repository.UserRepository;
import com.example.playmatch.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements com.example.playmatch.auth.service.AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${app.security.password-reset.expiration}")
    private long passwordResetExpirationSeconds;

    @Override
    @Transactional
    public AuthUser registerUser(RegisterRequest request) {
        // Check if email exists
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        // Create new user
        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail().toLowerCase())
            .gender(Gender.valueOf(request.getGender().name()))  // Fixed: Convert enum properly
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .passwordUpdatedAt(OffsetDateTime.now())
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();

        User savedUser = userRepository.save(user);

        // Map to API response
        return mapToAuthUser(savedUser);
    }

    @Override
    @Transactional
    public LoginResponse authenticateUser(LoginRequest request) {
        try {
            // Attempt authentication
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail().toLowerCase(),
                    request.getPassword()
                )
            );

            User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            // Update last login time and reset failed attempts
            userRepository.updateLoginSuccess(user.getId(), OffsetDateTime.now());

            // Generate tokens
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            // Create response
            return new LoginResponse()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(3600)
                .refreshToken(refreshToken)
                .user(mapToAuthUser(user));

        } catch (BadCredentialsException e) {
            User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            // Increment failed attempts and possibly lock account
            handleFailedLogin(user);
            throw e;
        }
    }

    @Override
    @Transactional
    public void initiatePasswordReset(String email) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            // Invalidate any existing tokens
            tokenRepository.invalidateUserTokens(user.getId(), OffsetDateTime.now());

            // Create new reset token
            String rawToken = UUID.randomUUID().toString();
            log.info("Generated password reset token for user {}: {}", email, rawToken);

            PasswordResetToken token = PasswordResetToken.builder()
                .userId(user.getId())
                .tokenHash(passwordEncoder.encode(rawToken))
                .issuedAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusSeconds(passwordResetExpirationSeconds))
                .build();

            tokenRepository.save(token);

            // TODO: Send email with reset instructions
            // Email sending would be implemented here
        });
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        // Find and validate the token using the hash directly
        PasswordResetToken resetToken = tokenRepository.findValidToken(token, OffsetDateTime.now())
            .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        // Find the associated user
        User user = userRepository.findById(resetToken.getUserId())
            .orElseThrow(() -> new InvalidTokenException("User not found"));

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);

        // Mark token as consumed
        resetToken.setConsumedAt(OffsetDateTime.now());
        tokenRepository.save(resetToken);
    }

    private void handleFailedLogin(User user) {
        short newFailedCount = (short) (user.getFailedLoginCount() + 1);
        OffsetDateTime lockoutTime = newFailedCount >= 5 ?
            OffsetDateTime.now().plusMinutes(15) : null;

        //TODO(BUG) : the failed login count is not updating after login failure
        userRepository.updateLoginFailure(user.getId(), lockoutTime);

        if (lockoutTime != null) {
            throw new AccountLockedException("Account temporarily locked due to too many failed attempts");
        }
    }

    private AuthUser mapToAuthUser(User user) {
        return new AuthUser()
            .id(UUID.fromString(user.getId().toString()))
            .name(user.getName())
            .gender(AuthUser.GenderEnum.valueOf(user.getGender().getValue().toUpperCase()))
            .email(user.getEmail())
            .createdAt(OffsetDateTime.parse(user.getCreatedAt().toString()))
            .updatedAt(OffsetDateTime.parse(user.getUpdatedAt().toString()))
            .lastLoginAt(user.getLastLoginAt() != null ? OffsetDateTime.parse(user.getLastLoginAt().toString()) : null);
    }
}
