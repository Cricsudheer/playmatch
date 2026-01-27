package com.example.playmatch.mvp.auth.controller;

import com.example.playmatch.mvp.auth.dto.OtpRequestDto;
import com.example.playmatch.mvp.auth.dto.OtpVerifyDto;
import com.example.playmatch.mvp.auth.dto.OtpVerifyResponseDto;
import com.example.playmatch.mvp.auth.dto.ProfileUpdateDto;
import com.example.playmatch.mvp.auth.dto.RefreshTokenRequestDto;
import com.example.playmatch.mvp.auth.dto.RefreshTokenResponseDto;
import com.example.playmatch.mvp.auth.security.CurrentMvpUser;
import com.example.playmatch.mvp.auth.service.OtpService;
import com.example.playmatch.mvp.common.error.MvpError;
import com.example.playmatch.mvp.common.exception.MvpException;
import com.example.playmatch.mvp.users.model.MvpUser;
import com.example.playmatch.mvp.users.repository.MvpUserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/mvp/auth")
@RequiredArgsConstructor
@Slf4j
public class MvpAuthController {

    private final OtpService otpService;
    private final MvpUserRepository mvpUserRepository;

    @PostMapping("/otp/request")
    public ResponseEntity<Void> requestOtp(@Valid @RequestBody OtpRequestDto request) {
        log.info("OTP request received for phone: {}", request.getPhoneNumber());
        otpService.requestOtp(request.getPhoneNumber());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<OtpVerifyResponseDto> verifyOtp(@Valid @RequestBody OtpVerifyDto request) {
        log.info("OTP verification request for phone: {}", request.getPhoneNumber());

        OtpService.OtpVerificationResult result = otpService.verifyOtp(
            request.getPhoneNumber(),
            request.getOtpCode()
        );

        OtpVerifyResponseDto response = OtpVerifyResponseDto.builder()
            .accessToken(result.accessToken())
            .refreshToken(result.refreshToken())
            .userId(result.userId())
            .phoneNumber(result.phoneNumber())
            .name(result.name())
            .requiresProfile(result.requiresProfile())
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token using a valid refresh token.
     * This endpoint does not require authentication as it uses the refresh token itself.
     *
     * @param request Request containing the refresh token
     * @return New access token and refresh token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        log.info("Refresh token request received");

        OtpService.RefreshTokenResult result = otpService.refreshAccessToken(request.getRefreshToken());

        RefreshTokenResponseDto response = RefreshTokenResponseDto.builder()
            .accessToken(result.accessToken())
            .refreshToken(result.refreshToken())
            .userId(result.userId())
            .phoneNumber(result.phoneNumber())
            .name(result.name())
            .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/profile")
    public ResponseEntity<Void> updateProfile(@Valid @RequestBody ProfileUpdateDto request) {
        Long userId = CurrentMvpUser.getUserId();
        log.info("Profile update request for userId: {}", userId);

        MvpUser user = mvpUserRepository.findById(userId)
            .orElseThrow(() -> new MvpException(MvpError.USER_NOT_FOUND));

        user.setName(request.getName());
        if (request.getArea() != null && !request.getArea().isBlank()) {
            user.setArea(request.getArea());
        }

        mvpUserRepository.save(user);

        log.info("Profile updated successfully for userId: {}", userId);
        return ResponseEntity.noContent().build();
    }
}
