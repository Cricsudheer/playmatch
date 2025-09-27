package com.example.playmatch.auth.controller;

import com.example.playmatch.api.controller.AuthApi;
import com.example.playmatch.api.model.*;
import com.example.playmatch.auth.exception.PasswordMismatchException;
import com.example.playmatch.auth.security.AllowAnonymous;
import com.example.playmatch.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.OffsetDateTime;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @AllowAnonymous
    @Override
    public ResponseEntity<AuthUser> _register(@Valid @RequestBody RegisterRequest request) {
        log.info("Processing registration request for: {}", request.getEmail());

        // Validate password match
        if (!request.getPassword().equals(request.getReEnterPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        AuthUser user = authService.registerUser(request);
        return ResponseEntity.status(201).body(user);
    }

    @AllowAnonymous
    @Override
    public ResponseEntity<LoginResponse> _login(@Valid @RequestBody LoginRequest request) {
        log.info("Processing login request for: {}", request.getEmail());
        LoginResponse response = authService.authenticateUser(request);
        return ResponseEntity.ok(response);
    }

    @AllowAnonymous
    @Override
    public ResponseEntity<MessageResponse> _forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        // Don't log the actual email to prevent user enumeration in logs
        log.info("Processing forgot password request");
        authService.initiatePasswordReset(request.getEmail());

        // Always return 202 to prevent user enumeration
        MessageResponse response = new MessageResponse()
            .message("If an account exists, instructions were sent.");
        return ResponseEntity.accepted().body(response);
    }

    @AllowAnonymous
    @Override
    public ResponseEntity<MessageResponse> _resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Processing password reset request");

        // Validate password match
        if (!request.getPassword().equals(request.getReEnterPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        authService.resetPassword(request.getToken(), request.getPassword());

        MessageResponse response = new MessageResponse()
            .message("Password updated successfully.");
        return ResponseEntity.ok(response);
    }
}
