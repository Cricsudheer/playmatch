package com.example.playmatch.team.exception;

import com.example.playmatch.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum TeamError implements ErrorCode {
  TEAM_NOT_FOUND("TM-TEAM-404", "Team not found", HttpStatus.NOT_FOUND),
  TEAM_ALREADY_EXISTS("TM-TEAM-409", "Team already exists", HttpStatus.CONFLICT),
  TEAM_DELETED("TM-TEAM-410", "Team is deleted", HttpStatus.GONE),
  MEMBER_LIMIT_REACHED("TM-TEAM-422", "Team member limit reached", HttpStatus.UNPROCESSABLE_ENTITY),
  MEMBER_NOT_FOUND("TM-MEMBER-404", "Member not found in team", HttpStatus.NOT_FOUND);

  private final String code;
  private final String title;
  private final HttpStatus status;

  TeamError(String code, String title, HttpStatus status) {
    this.code = code; this.title = title; this.status = status;
  }
  public String code() { return code; }
  public String title() { return title; }
  public HttpStatus status() { return status; }
}