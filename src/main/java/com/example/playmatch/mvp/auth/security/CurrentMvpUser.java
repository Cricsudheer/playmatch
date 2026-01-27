package com.example.playmatch.mvp.auth.security;

import com.example.playmatch.mvp.common.error.MvpError;
import com.example.playmatch.mvp.common.exception.MvpException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentMvpUser {
    public static Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof MvpUserPrincipal) {
            return ((MvpUserPrincipal) authentication.getPrincipal()).getId();
        }
        throw new MvpException(MvpError.UNAUTHORIZED);
    }

    public static String getPhoneNumber() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof MvpUserPrincipal) {
            return ((MvpUserPrincipal) authentication.getPrincipal()).getPhoneNumber();
        }
        throw new MvpException(MvpError.UNAUTHORIZED);
    }

    public static MvpUserPrincipal getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof MvpUserPrincipal) {
            return (MvpUserPrincipal) authentication.getPrincipal();
        }
        throw new MvpException(MvpError.UNAUTHORIZED);
    }
}
