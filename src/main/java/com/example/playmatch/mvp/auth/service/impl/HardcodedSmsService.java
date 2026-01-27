package com.example.playmatch.mvp.auth.service.impl;

import com.example.playmatch.mvp.auth.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@Slf4j
public class HardcodedSmsService implements SmsService {
    private static final String HARDCODED_OTP = "123456";

    @Override
    public void sendOtp(String phoneNumber, String otpCode) {
        // For MVP: Always log the hardcoded OTP instead of sending SMS
        log.info("=".repeat(60));
        log.info("OTP for {}: {} (HARDCODED - MVP MODE)", phoneNumber, HARDCODED_OTP);
        log.info("Note: In production, this would send via SMS gateway");
        log.info("=".repeat(60));

        // In production, this would integrate with Twilio, AWS SNS, etc.
        // Example:
        // twilioClient.messages.create(
        //     new Message.Builder()
        //         .to(phoneNumber)
        //         .from(config.getFromNumber())
        //         .body("Your GameTeam OTP is: " + otpCode)
        //         .build()
        // );
    }

    public static String getHardcodedOtp() {
        return HARDCODED_OTP;
    }
}
