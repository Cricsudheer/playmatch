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

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    @Query("SELECT t FROM PasswordResetToken t WHERE t.tokenHash = :hash AND t.consumedAt IS NULL AND t.expiresAt > :now")
    Optional<PasswordResetToken> findValidToken(@Param("hash") String hash, @Param("now") OffsetDateTime now);

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.consumedAt = :now WHERE t.userId = :userId AND t.consumedAt IS NULL")
    void invalidateUserTokens(@Param("userId") UUID userId, @Param("now") OffsetDateTime now);

    Optional<PasswordResetToken> findFirstByUserIdOrderByIssuedAtDesc(UUID userId);
}
