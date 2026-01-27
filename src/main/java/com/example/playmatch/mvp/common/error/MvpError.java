package com.example.playmatch.mvp.common.error;

import com.example.playmatch.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum MvpError implements ErrorCode {
    // Auth errors
    INVALID_OTP("MVP-AUTH-001", "Invalid OTP code", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED("MVP-AUTH-002", "OTP has expired", HttpStatus.BAD_REQUEST),
    OTP_MAX_ATTEMPTS("MVP-AUTH-003", "Maximum OTP attempts exceeded", HttpStatus.TOO_MANY_REQUESTS),
    OTP_RATE_LIMIT_EXCEEDED("MVP-AUTH-004", "OTP rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS),
    PROFILE_INCOMPLETE("MVP-AUTH-005", "User profile is incomplete", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_NUMBER("MVP-AUTH-006", "Invalid phone number format", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN("MVP-AUTH-007", "Invalid or expired token", HttpStatus.UNAUTHORIZED),

    // Match errors
    MATCH_NOT_FOUND("MVP-MATCH-001", "Match not found", HttpStatus.NOT_FOUND),
    MATCH_FULL("MVP-MATCH-002", "Match is full", HttpStatus.BAD_REQUEST),
    MATCH_ALREADY_COMPLETED("MVP-MATCH-003", "Match is already completed", HttpStatus.BAD_REQUEST),
    MATCH_CANCELLED("MVP-MATCH-004", "Match has been cancelled", HttpStatus.BAD_REQUEST),
    INVALID_MATCH_STATUS("MVP-MATCH-005", "Invalid match status for this operation", HttpStatus.BAD_REQUEST),

    // Invite errors
    INVITE_NOT_FOUND("MVP-INVITE-001", "Invite not found", HttpStatus.NOT_FOUND),
    INVITE_EXPIRED("MVP-INVITE-002", "Invite has expired", HttpStatus.BAD_REQUEST),
    INVITE_GENERATION_FAILED("MVP-INVITE-003", "Failed to generate unique invite token", HttpStatus.INTERNAL_SERVER_ERROR),

    // Emergency errors
    EMERGENCY_REQUEST_NOT_FOUND("MVP-EMERGENCY-001", "Emergency request not found", HttpStatus.NOT_FOUND),
    EMERGENCY_ALREADY_REQUESTED("MVP-EMERGENCY-002", "User already has an active emergency request", HttpStatus.BAD_REQUEST),
    EMERGENCY_LOCK_EXPIRED("MVP-EMERGENCY-003", "Emergency request lock has expired", HttpStatus.BAD_REQUEST),
    EMERGENCY_NOT_ENABLED("MVP-EMERGENCY-004", "Emergency requests not enabled for this match", HttpStatus.BAD_REQUEST),
    EMERGENCY_ALREADY_PROCESSED("MVP-EMERGENCY-005", "Emergency request already processed", HttpStatus.BAD_REQUEST),

    // Authorization errors
    NOT_CAPTAIN("MVP-AUTH-010", "Only the match captain can perform this action", HttpStatus.FORBIDDEN),
    UNAUTHORIZED("MVP-AUTH-011", "Unauthorized access", HttpStatus.UNAUTHORIZED),

    // Participant errors
    PARTICIPANT_NOT_FOUND("MVP-PARTICIPANT-001", "Participant not found", HttpStatus.NOT_FOUND),
    ALREADY_RESPONDED("MVP-PARTICIPANT-002", "User has already responded to this match", HttpStatus.BAD_REQUEST),

    // User errors
    USER_NOT_FOUND("MVP-USER-001", "User not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String title;
    private final HttpStatus status;

    MvpError(String code, String title, HttpStatus status) {
        this.code = code;
        this.title = title;
        this.status = status;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public HttpStatus status() {
        return status;
    }
}
