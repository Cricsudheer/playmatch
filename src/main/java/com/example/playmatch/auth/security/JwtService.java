package com.example.playmatch.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.security.jwt.secret-key}")
    private String secretKey;

    @Value("${app.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    private static final String USER_ID_CLAIM = "uid";
    private static final String ENABLED_CLAIM = "enabled";
    private static final String ACCOUNT_NON_LOCKED_CLAIM = "accountNonLocked";

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        String userIdStr = extractClaim(token, claims -> claims.get(USER_ID_CLAIM, String.class));
        return userIdStr != null ? Long.parseLong(userIdStr) : null;
    }

    public Boolean extractEnabled(String token) {
        return extractClaim(token, claims -> claims.get(ENABLED_CLAIM, Boolean.class));
    }

    public Boolean extractAccountNonLocked(String token) {
        return extractClaim(token, claims -> claims.get(ACCOUNT_NON_LOCKED_CLAIM, Boolean.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generate access token from UserPrincipal
     */
    public String generateToken(UserPrincipal userPrincipal) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(USER_ID_CLAIM, userPrincipal.getUserId().toString());
        claims.put(ENABLED_CLAIM, userPrincipal.isEnabled());
        claims.put(ACCOUNT_NON_LOCKED_CLAIM, userPrincipal.isAccountNonLocked());
        return buildToken(claims, userPrincipal.getEmail(), jwtExpiration);
    }

    /**
     * Generate refresh token from UserPrincipal
     */
    public String generateRefreshToken(UserPrincipal userPrincipal) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(USER_ID_CLAIM, userPrincipal.getUserId().toString());
        claims.put(ENABLED_CLAIM, userPrincipal.isEnabled());
        claims.put(ACCOUNT_NON_LOCKED_CLAIM, userPrincipal.isAccountNonLocked());
        return buildToken(claims, userPrincipal.getEmail(), refreshExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusMillis(expiration)))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validate token by checking expiration only (username already verified during extraction)
     */
    public boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }

    /**
     * Build UserPrincipal directly from JWT claims
     */
    public UserPrincipal extractUserPrincipal(String token) {
        Claims claims = extractAllClaims(token);

        Long userId = Long.parseLong(claims.get(USER_ID_CLAIM, String.class));
        String email = claims.getSubject();
        Boolean enabled = claims.get(ENABLED_CLAIM, Boolean.class);
        Boolean accountNonLocked = claims.get(ACCOUNT_NON_LOCKED_CLAIM, Boolean.class);

        return new UserPrincipal(
            userId,
            email,
            null, // password not needed for JWT auth
            enabled != null ? enabled : true,
            accountNonLocked != null ? accountNonLocked : true
        );
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

}
