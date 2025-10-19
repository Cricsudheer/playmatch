package com.example.playmatch.auth.security;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility for accessing the authenticated user without duplicating boilerplate.
 */
public final class CurrentUser {

    private CurrentUser() {}

    /**
     * Returns the UserPrincipal if authenticated, otherwise null.
     */
    public static UserPrincipal getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal up) {
            return up;
        }
        return null;
    }

    /**
     * Returns the authenticated user's UUID or null if missing.
     */
    public static UUID getUserId() {
        UserPrincipal up = getPrincipal();
        return up != null ? up.getId() : null;
    }

    /**
     * Returns userId or throws IllegalStateException if absent.
     */
    public static UUID requireUserId() {
        UUID id = getUserId();
        if (id == null) {
            throw new IllegalStateException("No authenticated user in security context");
        }
        return id;
    }
}

