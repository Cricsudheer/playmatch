package com.example.playmatch.common.error;

import com.example.playmatch.common.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private static final String DOCS_BASE = "https://docs.cricketmanager.app/errors/";

  private ProblemDetail toProblem(ErrorCode code, String detail, HttpServletRequest req) {
    var body = ProblemDetail.forStatusAndDetail(code.status(), detail != null ? detail : code.title());
    body.setTitle(code.title());
    body.setType(URI.create(DOCS_BASE + code.code()));  // docs per code
    body.setInstance(URI.create(req.getRequestURI()));
    body.setProperty("code", code.code());
    body.setProperty("timestamp", OffsetDateTime.now().toString());
    body.setProperty("correlationId", Optional.ofNullable(req.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).orElse(""));
    return body;
  }

  // Your domain/app exceptions (module enums are accepted via the interface)
  @ExceptionHandler(AppException.class)
  public ProblemDetail handleApp(AppException ex, HttpServletRequest req) {
    log.warn("AppException [{}]: {}", ex.getError().code(), ex.getMessage());
    return toProblem(ex.getError(), ex.getDetails(), req);
  }

  // Validation
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    var pd = toProblem(StandardError.VALIDATION_FAILED, "Validation failed", req);
    var errors = new ArrayList<Map<String, String>>();
    ex.getBindingResult().getAllErrors().forEach(err -> {
      var map = new HashMap<String, String>();
      if (err instanceof FieldError fe) {
        map.put("field", fe.getField());
        map.put("message", Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid value"));
      } else {
        map.put("message", Optional.ofNullable(err.getDefaultMessage()).orElse(err.getObjectName()));
      }
      errors.add(map);
    });
    pd.setProperty("errors", errors);
    return pd;
  }

  // Security (if using Spring Security)
  @ExceptionHandler(AuthenticationException.class)
  public ProblemDetail handleAuth(AuthenticationException ex, HttpServletRequest req) {
    return toProblem(StandardError.UNAUTHENTICATED, ex.getMessage(), req);
  }
  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail handleForbidden(AccessDeniedException ex, HttpServletRequest req) {
    return toProblem(StandardError.FORBIDDEN, ex.getMessage(), req);
  }

  // Persistence conflicts
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ProblemDetail handleConflict(DataIntegrityViolationException ex, HttpServletRequest req) {
    log.warn("Data conflict: {}", ex.getMostSpecificCause().getMessage());
    return toProblem(StandardError.CONFLICT, "Conflict", req);
  }

  // Fallback
  @ExceptionHandler(Exception.class)
  public ProblemDetail handleAny(Exception ex, HttpServletRequest req) {
    log.error("Unhandled", ex);
    return toProblem(StandardError.INTERNAL_ERROR, "Something went wrong", req);
  }
}