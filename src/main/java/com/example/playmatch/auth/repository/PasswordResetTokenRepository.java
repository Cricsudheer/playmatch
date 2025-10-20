package com.example.playmatch.auth.repository;

import com.example.playmatch.auth.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHashAndConsumedAtIsNull(String tokenHash);

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.consumedAt = :now WHERE t.userId = :userId")
    void invalidateUserTokens(@Param("userId") Long userId, @Param("now") OffsetDateTime now);

    Optional<PasswordResetToken> findFirstByUserIdOrderByIssuedAtDesc(Long userId);
}
