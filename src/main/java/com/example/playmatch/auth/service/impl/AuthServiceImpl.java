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
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        // Create new user
        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail().toLowerCase())
            .gender(Gender.valueOf(request.getGender().name()))
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

            User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            // Update last login time and reset failed attempts
            userRepository.updateLastLogin(user.getId(), OffsetDateTime.now());

            return generateTokenResponse(user);

        } catch (BadCredentialsException e) {
            User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

            if (user != null) {
                // Increment failed attempts and possibly lock account
                handleFailedLogin(user);
            }
            throw e;
        }
    }

    @Override
    @Transactional
    public LoginResponse refreshAccessToken(String refreshToken) {
        try {
            // Extract username from refresh token
            String username = jwtService.extractUsername(refreshToken);

            // Find user
            User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidTokenException("User not found"));

            // Validate refresh token
            if (!jwtService.isTokenValid(refreshToken, user)) {
                throw new InvalidTokenException("Invalid refresh token");
            }

            // Generate new tokens
            return generateTokenResponse(user);

        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid refresh token");
        }
    }

    @Override
    @Transactional
    public void initiatePasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            // Invalidate any existing tokens
            tokenRepository.invalidateUserTokens(user.getId(), OffsetDateTime.now());

            // Create new reset token
            String rawToken = java.util.UUID.randomUUID().toString();
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
        PasswordResetToken resetToken = tokenRepository.findByTokenHashAndConsumedAtIsNull(token)
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

    private LoginResponse generateTokenResponse(User user) {
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
    }

    private void handleFailedLogin(User user) {
        short newFailedCount = (short) (user.getFailedLoginCount() + 1);
        OffsetDateTime lockoutTime = newFailedCount >= 5 ?
            OffsetDateTime.now().plusMinutes(15) : null;

        userRepository.updateFailedLoginCount(user.getId(), newFailedCount, lockoutTime);

        if (lockoutTime != null) {
            throw new AccountLockedException("Account temporarily locked due to too many failed attempts");
        }
    }

    private AuthUser mapToAuthUser(User user) {
        return new AuthUser()
            .id(user.getId())
            .name(user.getName())
            .gender(AuthUser.GenderEnum.valueOf(user.getGender().name()))
            .email(user.getEmail())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .lastLoginAt(user.getLastLoginAt() != null ? OffsetDateTime.parse(user.getLastLoginAt().toString()) : null);
    }
}
