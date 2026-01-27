package com.example.playmatch.mvp.auth.service.impl;

import com.example.playmatch.auth.security.JwtService;
import com.example.playmatch.mvp.auth.model.OtpRateLimit;
import com.example.playmatch.mvp.auth.model.OtpVerification;
import com.example.playmatch.mvp.auth.repository.OtpRateLimitRepository;
import com.example.playmatch.mvp.auth.repository.OtpVerificationRepository;
import com.example.playmatch.mvp.auth.service.OtpService;
import com.example.playmatch.mvp.auth.service.SmsService;
import com.example.playmatch.mvp.common.error.MvpError;
import com.example.playmatch.mvp.common.exception.MvpException;
import com.example.playmatch.mvp.users.model.MvpUser;
import com.example.playmatch.mvp.users.repository.MvpUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final OtpVerificationRepository otpVerificationRepository;
    private final OtpRateLimitRepository otpRateLimitRepository;
    private final MvpUserRepository mvpUserRepository;
    private final SmsService smsService;
    private final JwtService jwtService;

    @Value("${app.mvp.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Value("${app.mvp.otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.mvp.otp.rate-limit-window-minutes:10}")
    private int rateLimitWindowMinutes;

    @Value("${app.mvp.otp.max-requests-per-window:3}")
    private int maxRequestsPerWindow;

    @Override
    @Transactional
    public void requestOtp(String phoneNumber) {
        // Validate phone number format
        if (!isValidPhoneNumber(phoneNumber)) {
            throw new MvpException(MvpError.INVALID_PHONE_NUMBER);
        }

        // Check rate limiting
        checkRateLimit(phoneNumber);

        // Generate hardcoded OTP for MVP
        String otpCode = HardcodedSmsService.getHardcodedOtp();

        // Store OTP verification record
        OtpVerification otpVerification = OtpVerification.builder()
            .phoneNumber(phoneNumber)
            .otpCode(otpCode) // In production, hash this
            .attempts(0)
            .verified(false)
            .expiresAt(OffsetDateTime.now().plusMinutes(otpExpiryMinutes))
            .build();

        otpVerificationRepository.save(otpVerification);

        // Send OTP via SMS service (logs in MVP mode)
        smsService.sendOtp(phoneNumber, otpCode);

        // Update rate limit counter
        updateRateLimit(phoneNumber);

        log.info("OTP requested for phone number: {}", phoneNumber);
    }

    @Override
    @Transactional
    public OtpVerificationResult verifyOtp(String phoneNumber, String otpCode) {
        // Find the most recent unverified OTP for this phone number
        OtpVerification otpVerification = otpVerificationRepository
            .findFirstByPhoneNumberAndVerifiedFalseOrderByCreatedAtDesc(phoneNumber)
            .orElseThrow(() -> new MvpException(MvpError.INVALID_OTP, "No pending OTP found"));

        // Check if OTP has expired
        if (otpVerification.isExpired()) {
            throw new MvpException(MvpError.OTP_EXPIRED);
        }

        // Check max attempts
        if (otpVerification.getAttempts() >= maxAttempts) {
            throw new MvpException(MvpError.OTP_MAX_ATTEMPTS);
        }

        // Increment attempts
        otpVerification.incrementAttempts();
        otpVerificationRepository.save(otpVerification);

        // Verify OTP code
        if (!otpVerification.getOtpCode().equals(otpCode)) {
            log.warn("Invalid OTP attempt for phone: {} (attempt {}/{})",
                phoneNumber, otpVerification.getAttempts(), maxAttempts);
            throw new MvpException(MvpError.INVALID_OTP);
        }

        // Mark as verified
        otpVerification.setVerified(true);
        otpVerificationRepository.save(otpVerification);

        // Find or create MVP user
        MvpUser mvpUser = mvpUserRepository.findByPhoneNumber(phoneNumber)
            .orElseGet(() -> createNewMvpUser(phoneNumber));

        // Generate JWT tokens
        String accessToken = jwtService.generateMvpAccessToken(
            mvpUser.getId(),
            mvpUser.getPhoneNumber(),
            mvpUser.getName()
        );

        String refreshToken = jwtService.generateMvpRefreshToken(
            mvpUser.getId(),
            mvpUser.getPhoneNumber()
        );

        log.info("OTP verified successfully for phone: {}, userId: {}", phoneNumber, mvpUser.getId());

        // Return result with requiresProfile flag
        return new OtpVerificationResult(
            accessToken,
            refreshToken,
            mvpUser.getId(),
            mvpUser.getPhoneNumber(),
            mvpUser.getName(),
            mvpUser.getName() == null || mvpUser.getName().isBlank()
        );
    }

    private void checkRateLimit(String phoneNumber) {
        Optional<OtpRateLimit> rateLimitOpt = otpRateLimitRepository.findByPhoneNumber(phoneNumber);

        if (rateLimitOpt.isPresent()) {
            OtpRateLimit rateLimit = rateLimitOpt.get();

            // Check if still within the rate limit window
            if (rateLimit.isWithinWindow(rateLimitWindowMinutes)) {
                if (rateLimit.getRequestCount() >= maxRequestsPerWindow) {
                    log.warn("Rate limit exceeded for phone: {}", phoneNumber);
                    throw new MvpException(MvpError.OTP_RATE_LIMIT_EXCEEDED);
                }
            } else {
                // Window expired, reset counter
                rateLimit.resetWindow();
                otpRateLimitRepository.save(rateLimit);
            }
        }
    }

    private void updateRateLimit(String phoneNumber) {
        Optional<OtpRateLimit> rateLimitOpt = otpRateLimitRepository.findByPhoneNumber(phoneNumber);

        if (rateLimitOpt.isPresent()) {
            OtpRateLimit rateLimit = rateLimitOpt.get();

            if (rateLimit.isWithinWindow(rateLimitWindowMinutes)) {
                rateLimit.incrementCount();
            } else {
                rateLimit.resetWindow();
            }

            otpRateLimitRepository.save(rateLimit);
        } else {
            // Create new rate limit record
            OtpRateLimit newRateLimit = OtpRateLimit.builder()
                .phoneNumber(phoneNumber)
                .requestCount(1)
                .windowStart(OffsetDateTime.now())
                .build();

            otpRateLimitRepository.save(newRateLimit);
        }
    }

    private MvpUser createNewMvpUser(String phoneNumber) {
        MvpUser newUser = MvpUser.builder()
            .phoneNumber(phoneNumber)
            .name(null) // Will be set later via profile update
            .area(null)
            .build();

        return mvpUserRepository.save(newUser);
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Basic validation: starts with + and has 10-15 digits
        return phoneNumber != null &&
               phoneNumber.matches("^\\+\\d{10,15}$");
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshTokenResult refreshAccessToken(String refreshToken) {
        try {
            // Validate the refresh token
            if (!jwtService.isTokenValid(refreshToken)) {
                log.warn("Invalid or expired refresh token");
                throw new MvpException(MvpError.INVALID_TOKEN);
            }

            // Check if it's an MVP token
            if (!jwtService.isMvpUserToken(refreshToken)) {
                log.warn("Refresh token is not an MVP user token");
                throw new MvpException(MvpError.INVALID_TOKEN);
            }

            // Extract user info from refresh token
            Long userId = jwtService.extractMvpUserId(refreshToken);
            String phoneNumber = jwtService.extractPhoneNumber(refreshToken);

            if (userId == null || phoneNumber == null) {
                log.warn("Could not extract user info from refresh token");
                throw new MvpException(MvpError.INVALID_TOKEN);
            }

            // Verify user still exists
            MvpUser mvpUser = mvpUserRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for refresh token: userId={}", userId);
                    return new MvpException(MvpError.USER_NOT_FOUND);
                });

            // Generate new tokens
            String newAccessToken = jwtService.generateMvpAccessToken(
                mvpUser.getId(),
                mvpUser.getPhoneNumber(),
                mvpUser.getName()
            );

            String newRefreshToken = jwtService.generateMvpRefreshToken(
                mvpUser.getId(),
                mvpUser.getPhoneNumber()
            );

            log.info("Access token refreshed for userId: {}", userId);

            return new RefreshTokenResult(
                newAccessToken,
                newRefreshToken,
                mvpUser.getId(),
                mvpUser.getPhoneNumber(),
                mvpUser.getName()
            );

        } catch (MvpException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error refreshing access token", e);
            throw new MvpException(MvpError.INVALID_TOKEN);
        }
    }
}
