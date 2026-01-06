package com.example.playmatch.auth.repository;

import com.example.playmatch.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    /**
     * Update user's failed login count atomically
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginCount = :count, u.lockoutUntil = :lockoutUntil, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void updateFailedLoginCount(
        @Param("userId") Long userId,
        @Param("count") Short count,
        @Param("lockoutUntil") OffsetDateTime lockoutUntil
    );

    /**
     * Update user's last login timestamp
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt, u.failedLoginCount = 0, u.lockoutUntil = null, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void updateLastLogin(
        @Param("userId") Long userId,
        @Param("lastLoginAt") OffsetDateTime lastLoginAt
    );
}
