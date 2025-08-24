package com.example.playmatch.security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Optional;

public class JwtUtils {
    // Store securely (Vault/Secret Manager). 32+ chars for HS256.
    public static Key key(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public static Optional<Jws<Claims>> parse(String token, String secret) {
        try {
            return Optional.of(Jwts.parserBuilder()
                    .setSigningKey(key(secret))
                    .build()
                    .parseClaimsJws(token));
        } catch (JwtException e) {
            return Optional.empty();
        }
    }

    // (No issuer/audience/business claims here by design)
    public static boolean isExpired(Claims claims) {
        Date exp = claims.getExpiration();
        return exp != null && exp.before(new Date());
    }
}
