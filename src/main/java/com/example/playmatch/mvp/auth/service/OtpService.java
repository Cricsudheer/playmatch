package com.example.playmatch.mvp.auth.service;

import com.example.playmatch.mvp.users.model.MvpUser;

public interface OtpService {
    /**
     * Request OTP for phone number. Checks rate limits and generates OTP.
     *
     * @param phoneNumber Phone number with country code (e.g., +919876543210)
     */
    void requestOtp(String phoneNumber);

    /**
     * Verify OTP code for phone number. Creates/finds user and returns tokens.
     *
     * @param phoneNumber Phone number with country code
     * @param otpCode     The OTP code to verify
     * @return Verification result containing tokens and user info
     */
    OtpVerificationResult verifyOtp(String phoneNumber, String otpCode);

    /**
     * Refresh access token using a valid refresh token.
     *
     * @param refreshToken The refresh token
     * @return New tokens and user info
     */
    RefreshTokenResult refreshAccessToken(String refreshToken);

    /**
     * Result of OTP verification
     */
    record OtpVerificationResult(
        String accessToken,
        String refreshToken,
        Long userId,
        String phoneNumber,
        String name,
        boolean requiresProfile
    ) {}

    /**
     * Result of refresh token operation
     */
    record RefreshTokenResult(
        String accessToken,
        String refreshToken,
        Long userId,
        String phoneNumber,
        String name
    ) {}
}
