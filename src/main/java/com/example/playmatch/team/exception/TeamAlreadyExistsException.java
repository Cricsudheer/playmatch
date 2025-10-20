package com.example.playmatch.team.exception;

public class TeamAlreadyExistsException extends RuntimeException {
    public TeamAlreadyExistsException(String message) {
        super(message);
    }

    public static TeamAlreadyExistsException forTeamName(String teamName) {
        return new TeamAlreadyExistsException("Team with name '" + teamName + "' already exists");
    }
}
