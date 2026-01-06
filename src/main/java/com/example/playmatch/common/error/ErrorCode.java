package com.example.playmatch.common.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
  String code();          // e.g., "TM-TEAM-404"
  String title();         // short, human title
  HttpStatus status();    // HTTP status to use
}