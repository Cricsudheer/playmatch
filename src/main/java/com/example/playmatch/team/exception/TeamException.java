package com.example.playmatch.team.exception;

import com.example.playmatch.common.error.AppException;

public class TeamException extends AppException {
  public TeamException(TeamError error) { super(error); }
  public TeamException(TeamError error, String details) { super(error, details); }
  public TeamException(TeamError error, String details, Throwable cause) { super(error, details, cause); }
}