package com.example.playmatch.auth.service;

import com.example.playmatch.api.model.*;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {
    /**
     * Register a new user
     * @param request Registration details
     * @return Created user details
     * @throws EmailAlreadyExistsException if email is already registered
     */
    AuthUser registerUser(RegisterRequest request);

    /**
     * Authenticate user and generate tokens
     * @param request Login credentials
     * @return Authentication response with tokens
     * @throws InvalidCredentialsException if credentials are invalid
     * @throws AccountLockedException if account is temporarily locked
     */
    LoginResponse authenticateUser(LoginRequest request);

    /**
     * Refresh access token using refresh token
     * @param refreshToken Valid refresh token
     * @return New authentication response with fresh tokens
     * @throws InvalidTokenException if refresh token is invalid or expired
     */
    LoginResponse refreshAccessToken(String refreshToken);

    /**
     * Initiate password reset process
     * @param email User's email address
     */
    void initiatePasswordReset(String email);

    /**
     * Complete password reset with token
     * @param token Reset token
     * @param newPassword New password
     * @throws InvalidTokenException if token is invalid or expired
     */
    void resetPassword(String token, String newPassword);
}
