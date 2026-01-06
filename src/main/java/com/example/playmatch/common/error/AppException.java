package com.example.playmatch.common.error;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppException extends RuntimeException {
  private final ErrorCode error;
  private final String details; // safe client-facing detail (optional)

  public AppException(ErrorCode error) {
    super(error.title());
    this.error = error;
    this.details = null;
  }

  public AppException(ErrorCode error, String details) {
    super(details != null ? details : error.title());
    this.error = error; this.details = details;
  }

  public AppException(ErrorCode error, String details, Throwable cause) {
    super(details != null ? details : error.title(), cause);
    this.error = error; this.details = details;
  }


}