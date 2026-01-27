package com.example.playmatch.mvp.auth.repository;

import com.example.playmatch.mvp.auth.model.OtpRateLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRateLimitRepository extends JpaRepository<OtpRateLimit, String> {
    Optional<OtpRateLimit> findByPhoneNumber(String phoneNumber);
}
