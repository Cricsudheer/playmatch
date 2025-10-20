package com.example.playmatch.team.exception;

public class TeamNotFoundException extends RuntimeException {
    public TeamNotFoundException(String message) {
        super(message);
    }

    public TeamNotFoundException(Long teamId) {
        super("Team not found with id: " + teamId);
    }
}
