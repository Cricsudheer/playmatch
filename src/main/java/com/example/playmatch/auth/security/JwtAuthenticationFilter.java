package com.example.playmatch.auth.security;

import com.example.playmatch.mvp.auth.security.MvpUserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    // Public endpoints that should skip JWT processing entirely
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/v1/auth/register",
        "/v1/auth/login",
        "/v1/auth/forgot-password",
        "/v1/auth/reset-password",
        "/v1/auth/refresh-token",
        "/v1/health/poll",
        "/v2/mvp/auth/otp/request",
        "/v2/mvp/auth/otp/verify",
        "/v2/mvp/auth/refresh-token",
        "/v2/mvp/invites",
        "/swagger-ui",
        "/v3/api-docs",
        "/actuator",
        "/error"
    );

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        log.debug("Processing request for path: {}", path);

        // Skip JWT processing for public endpoints
        if (isPublicPath(path)) {
            log.debug("Skipping JWT processing for public path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No valid Authorization header found");
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Check if this is an MVP user token or regular user token
                UserDetails principal;

                if (jwtService.isMvpUserToken(jwt)) {
                    // Extract MvpUserPrincipal from JWT claims
                    principal = jwtService.extractMvpUserPrincipal(jwt);
                    log.debug("Detected MVP user token");
                } else {
                    // Extract regular UserPrincipal from JWT claims
                    principal = jwtService.extractUserPrincipal(jwt);
                    log.debug("Detected regular user token");
                }

                // Validate token expiration
                if (jwtService.isTokenValid(jwt)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Successfully authenticated user from JWT: {}", principal.getUsername());
                }
            } catch (ExpiredJwtException e) {
                log.debug("Expired JWT encountered; proceeding unauthenticated: {}", e.getMessage());
            } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
                log.debug("Invalid JWT encountered; proceeding unauthenticated: {}", e.getMessage());
            } catch (Exception e) {
                log.debug("Unexpected JWT error; proceeding unauthenticated: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}
