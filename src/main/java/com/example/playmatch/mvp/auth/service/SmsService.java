package com.example.playmatch.mvp.auth.service;

public interface SmsService {
    /**
     * Send OTP code to the specified phone number
     *
     * @param phoneNumber The phone number (with country code)
     * @param otpCode     The OTP code to send
     */
    void sendOtp(String phoneNumber, String otpCode);
}
