package com.example.playmatch.auth.repository;

import com.example.playmatch.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find a user by their email address (case-insensitive).
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Find active (non-deleted) user by email for authentication.
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.isDeleted = false")
    Optional<User> findActiveByEmail(@Param("email") String email);

    /**
     * Check if an email is already registered.
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Update user's last login time and reset failed attempts.
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime, u.failedLoginCount = 0, u.lockoutUntil = null WHERE u.id = :userId")
    void updateLoginSuccess(@Param("userId") UUID userId, @Param("loginTime") OffsetDateTime loginTime);

    /**
     * Increment failed login attempts and optionally set lockout.
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginCount = u.failedLoginCount + 1, u.lockoutUntil = :lockoutUntil WHERE u.id = :userId")
    void updateLoginFailure(@Param("userId") UUID userId, @Param("lockoutUntil") OffsetDateTime lockoutUntil);

    /**
     * Update password related fields.
     */
    @Modifying
    @Query("UPDATE User u SET u.passwordHash = :hash, u.passwordAlgo = :algo, u.passwordUpdatedAt = :updateTime WHERE u.id = :userId")
    void updatePassword(@Param("userId") UUID userId, @Param("hash") String hash,
                       @Param("algo") String algo, @Param("updateTime") OffsetDateTime updateTime);
}
