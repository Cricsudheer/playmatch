package com.example.playmatch.auth.exception;

import com.example.playmatch.api.model.Problem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));

        Problem problem = new Problem()
            .type("https://api.playmatch.com/errors/validation")
            .title("Validation Error")
            .status(HttpStatus.BAD_REQUEST.value())
            .detail(details)
            .instance(request.getRequestURI());

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Problem> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Problem problem = new Problem()
            .type("https://api.playmatch.com/errors/validation")
            .title("Validation Error")
            .status(HttpStatus.BAD_REQUEST.value())
            .detail(ex.getMessage())
            .instance(request.getRequestURI());

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Problem> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        Problem problem = new Problem()
            .type("https://api.playmatch.com/errors/invalid-credentials")
            .title("Invalid Credentials")
            .status(HttpStatus.UNAUTHORIZED.value())
            .detail("The provided credentials are incorrect")
            .instance(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Problem> handleAccountLocked(LockedException ex, HttpServletRequest request) {
        Problem problem = new Problem()
            .type("https://api.playmatch.com/errors/account-locked")
            .title("Account Locked")
            .status(HttpStatus.LOCKED.value())
            .detail("Account is temporarily locked due to too many failed attempts")
            .instance(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.LOCKED).body(problem);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Problem> handleEmailExists(EmailAlreadyExistsException ex, HttpServletRequest request) {
        Problem problem = new Problem()
            .type("https://api.playmatch.com/errors/email-exists")
            .title("Email Already Registered")
            .status(HttpStatus.CONFLICT.value())
            .detail(ex.getMessage())
            .instance(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Problem> handleInvalidToken(InvalidTokenException ex, HttpServletRequest request) {
        Problem problem = new Problem()
            .type("https://api.playmatch.com/errors/invalid-token")
            .title("Invalid Token")
            .status(HttpStatus.GONE.value())
            .detail(ex.getMessage())
            .instance(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.GONE).body(problem);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<Problem> handlePasswordMismatch(PasswordMismatchException ex, HttpServletRequest request) {
        Problem problem = new Problem()
            .type("https://api.playmatch.com/errors/validation")
            .title("Password Mismatch")
            .status(HttpStatus.BAD_REQUEST.value())
            .detail(ex.getMessage())
            .instance(request.getRequestURI());

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Problem> handleUnexpectedError(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error occurred", ex);

        Problem problem = new Problem()
            .type("https://api.playmatch.com/errors/internal")
            .title("Internal Server Error")
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .detail("An unexpected error occurred")
            .instance(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
