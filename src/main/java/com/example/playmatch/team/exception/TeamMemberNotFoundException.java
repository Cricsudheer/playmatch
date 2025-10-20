package com.example.playmatch.team.exception;

public class TeamMemberNotFoundException extends RuntimeException {
    public TeamMemberNotFoundException(String message) {
        super(message);
    }

    public TeamMemberNotFoundException(Long teamId, Long userId) {
        super("Team member not found in team " + teamId + " for user " + userId);
    }
}
