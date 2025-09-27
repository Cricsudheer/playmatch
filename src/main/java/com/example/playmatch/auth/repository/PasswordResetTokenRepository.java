package com.example.playmatch.auth.repository;

import com.example.playmatch.auth.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for PasswordResetToken entity operations.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    /**
     * Find valid (non-consumed, non-expired) token by its hash.
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.tokenHash = :hash AND t.consumedAt IS NULL AND t.expiresAt > :now")
    Optional<PasswordResetToken> findValidToken(@Param("hash") String hash, @Param("now") OffsetDateTime now);

    /**
     * Find the latest token for a user.
     */
    Optional<PasswordResetToken> findFirstByUserIdOrderByIssuedAtDesc(UUID userId);

    /**
     * Invalidate all existing tokens for a user.
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.consumedAt = :now WHERE t.userId = :userId AND t.consumedAt IS NULL")
    void invalidateUserTokens(@Param("userId") UUID userId, @Param("now") OffsetDateTime now);

    /**
     * Mark a specific token as consumed.
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.consumedAt = :now WHERE t.id = :tokenId AND t.consumedAt IS NULL")
    void markTokenConsumed(@Param("tokenId") UUID tokenId, @Param("now") OffsetDateTime now);

    /**
     * Delete expired tokens for cleanup.
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :before")
    void deleteExpiredTokens(@Param("before") OffsetDateTime before);
}
