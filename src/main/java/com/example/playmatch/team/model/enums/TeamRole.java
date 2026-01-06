package com.example.playmatch.team.model.enums;

/**
 * Team membership role hierarchy.
 * ADMIN has full control; COORDINATOR manages operations; PLAYER is a regular member.
 */
public enum TeamRole {
    ADMIN,
    COORDINATOR,
    PLAYER;

    /**
     * Case-insensitive safe parse. Returns null if no match.
     */
    public static TeamRole fromString(String value) {
        if (value == null) return null;
        for (TeamRole r : values()) {
            if (r.name().equalsIgnoreCase(value)) {
                return r;
            }
        }
        return null;
    }

    /**
     * Validate that the provided role transition is allowed.
     * For now all transitions are allowed; hook for future business rules.
     */
    public boolean canAssignTo(TeamRole target) {
        return true; // placeholder for future restrictions
    }
}

