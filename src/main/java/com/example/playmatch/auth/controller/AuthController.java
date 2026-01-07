package com.example.playmatch.auth.controller;

import com.example.playmatch.api.controller.AuthApi;
import com.example.playmatch.api.model.*;
import com.example.playmatch.auth.exception.InvalidTokenException;
import com.example.playmatch.auth.exception.PasswordMismatchException;
import com.example.playmatch.auth.security.AllowAnonymous;
import com.example.playmatch.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.OffsetDateTime;
import java.util.Arrays;

@Slf4j
@Validated
@RestController
@RequestMapping("/v1/auth")  // Changed from /api/v1/auth to match the API interface
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Value("${app.security.cookie.enabled:true}")
    private boolean cookieEnabled;

    @Value("${app.security.cookie.domain:#{null}}")
    private String  cookieDomain;

    @Value("${app.security.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.security.jwt.refresh-token.expiration:604800000}")
    private long refreshTokenExpiration; // in milliseconds

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    @PostMapping("/register")
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

    @PostMapping("/login")
    @AllowAnonymous
    @Override
    public ResponseEntity<LoginResponse> _login(@Valid @RequestBody LoginRequest request) {
        log.info("Processing login request for: {}", request.getEmail());
        LoginResponse response = authService.authenticateUser(request);

        // Set refresh token as httpOnly cookie if enabled
        if (cookieEnabled && response.getRefreshToken() != null) {
            HttpServletResponse httpResponse =
                ((org.springframework.web.context.request.ServletRequestAttributes)
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
                .getResponse();
            if (httpResponse != null) {
                setRefreshTokenCookie(httpResponse, response.getRefreshToken());
            }
        }

        return ResponseEntity.ok(response);
    }
    

    @PostMapping("/forgot-password")
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

    @PostMapping("/reset-password")
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

    /**
     * Refresh access token using a refresh token.
     * The refresh token can be provided either:
     * 1. In the request body (JSON)
     * 2. As an httpOnly cookie (if cookie support is enabled)
     *
     * Cookie-based approach is preferred for better security against XSS attacks.
     *
     * @param request Optional request body containing refresh token
     * @return New access token and refresh token
     */
    @PostMapping("/refresh-token")
    @AllowAnonymous
    @Override
    public ResponseEntity<LoginResponse> _refreshToken(@Valid @RequestBody(required = false) RefreshTokenRequest request) {
        log.info("Processing refresh token request");

        // Try to get refresh token from request body first, then from cookie
        String refreshToken = null;

        if (request != null && StringUtils.hasText(request.getRefreshToken())) {
            refreshToken = request.getRefreshToken();
            log.debug("Using refresh token from request body");
        } else if (cookieEnabled) {
            // Try to extract from cookie using request context
            HttpServletRequest httpRequest =
                ((org.springframework.web.context.request.ServletRequestAttributes)
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
                .getRequest();
            refreshToken = extractRefreshTokenFromCookie(httpRequest);
            if (refreshToken != null) {
                log.debug("Using refresh token from cookie");
            }
        }

        // Validate that we have a refresh token
        if (!StringUtils.hasText(refreshToken)) {
            throw new InvalidTokenException("Refresh token is required");
        }

        // Generate new tokens
        LoginResponse response = authService.refreshAccessToken(refreshToken);

        // Set new refresh token as httpOnly cookie if enabled
        if (cookieEnabled && response.getRefreshToken() != null) {
            HttpServletResponse httpResponse =
                ((org.springframework.web.context.request.ServletRequestAttributes)
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
                .getResponse();
            if (httpResponse != null) {
                setRefreshTokenCookie(httpResponse, response.getRefreshToken());
            }
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Extract refresh token from httpOnly cookie.
     *
     * @param request HTTP request
     * @return Refresh token or null if not found
     */
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }

    /**
     * Set refresh token as httpOnly cookie in response.
     *
     * @param response HTTP response
     * @param refreshToken The refresh token value
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)  // Prevent JavaScript access (XSS protection)
                .secure(cookieSecure)  // Only send over HTTPS in production
                .path("/v1/auth")  // Limit cookie to auth endpoints
                .maxAge(refreshTokenExpiration / 1000)  // Convert milliseconds to seconds
                .sameSite("Strict")  // CSRF protection
                .domain(cookieDomain)  // Set domain if configured
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        log.debug("Refresh token cookie set with maxAge: {} seconds", refreshTokenExpiration / 1000);
    }

}
