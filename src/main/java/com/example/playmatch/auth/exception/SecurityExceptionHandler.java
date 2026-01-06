package com.example.playmatch.auth.exception;

import com.example.playmatch.api.model.Problem;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Problem> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        Problem problem = new Problem()
            .type("https://api.playmatch.com/errors/invalid-credentials")
            .title("Invalid credentials")
            .status(HttpStatus.UNAUTHORIZED.value())
            .detail("The provided credentials are incorrect")
            .instance(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Problem> handleAccountLocked(LockedException ex, HttpServletRequest request) {
        Problem problem = new Problem()
            .type("https://api.playmatch.com/errors/account-locked")
            .title("Account temporarily locked")
            .status(HttpStatus.LOCKED.value())
            .detail("Account is temporarily locked due to too many failed attempts")
            .instance(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.LOCKED).body(problem);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Problem> handleEmailExists(EmailAlreadyExistsException ex, HttpServletRequest request) {
        Problem problem = new Problem()
            .type("https://api.playmatch.com/errors/email-exists")
            .title("Email already registered")
            .status(HttpStatus.CONFLICT.value())
            .detail("An account with this email already exists")
            .instance(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Problem> handleInvalidToken(InvalidTokenException ex, HttpServletRequest request) {
        Problem problem = new Problem()
            .type("https://api.playmatch.com/errors/invalid-token")
            .title("Invalid or expired token")
            .status(HttpStatus.GONE.value())
            .detail("The provided token is invalid or has expired")
            .instance(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.GONE).body(problem);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<Problem> handlePasswordMismatch(PasswordMismatchException ex, HttpServletRequest request) {
        Problem problem = new Problem()
            .type("https://api.playmatch.com/errors/password-mismatch")
            .title("Password mismatch")
            .status(HttpStatus.BAD_REQUEST.value())
            .detail("The provided passwords do not match")
            .instance(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }
}
