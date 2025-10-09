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

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("SELECT u FROM User u WHERE u.email = LOWER(:email) AND u.isDeleted = false")
    Optional<User> findActiveByEmail(@Param("email") String email);

    @Modifying
    @Query("""
        UPDATE User u 
        SET u.lastLoginAt = :timestamp,
            u.failedLoginCount = 0,
            u.lockoutUntil = null
        WHERE u.id = :userId
        """)
    void updateLoginSuccess(
        @Param("userId") UUID userId,
        @Param("timestamp") OffsetDateTime timestamp
    );

    @Modifying
    @Query("""
        UPDATE User u 
        SET u.failedLoginCount = u.failedLoginCount + 1,
            u.lockoutUntil = :lockoutUntil
        WHERE u.id = :userId
        """)
    void updateLoginFailure(
        @Param("userId") UUID userId,
        @Param("lockoutUntil") OffsetDateTime lockoutUntil
    );
}
