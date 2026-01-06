package com.example.playmatch.common.error;

import org.springframework.http.HttpStatus;

public enum StandardError implements ErrorCode {
  VALIDATION_FAILED("CM-REQ-422", "Validation failed", HttpStatus.UNPROCESSABLE_ENTITY),
  UNAUTHENTICATED("CM-AUTH-401", "Authentication required", HttpStatus.UNAUTHORIZED),
  FORBIDDEN("CM-AUTH-403", "Forbidden", HttpStatus.FORBIDDEN),
  CONFLICT("CM-DB-409", "Conflict", HttpStatus.CONFLICT),
  INTERNAL_ERROR("CM-INT-500", "Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code; private final String title; private final HttpStatus status;
  StandardError(String code, String title, HttpStatus status) { this.code=code; this.title=title; this.status=status; }
  public String code() { return code; }
  public String title() { return title; }
  public HttpStatus status() { return status; }
}