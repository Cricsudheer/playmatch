package com.example.playmatch.auth.security;

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
     * Returns the authenticated user's ID or null if missing.
     */
    public static Long getUserId() {
        UserPrincipal principal = getPrincipal();
        return principal != null ? principal.getUserId() : null;
    }

    /**
     * Returns the authenticated user's email or null if missing.
     */
    public static String getEmail() {
        UserPrincipal principal = getPrincipal();
        return principal != null ? principal.getEmail() : null;
    }
}
