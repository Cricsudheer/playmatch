package com.example.playmatch.mvp.auth.repository;

import com.example.playmatch.mvp.auth.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findFirstByPhoneNumberAndVerifiedFalseOrderByCreatedAtDesc(String phoneNumber);

    List<OtpVerification> findByPhoneNumberAndVerifiedFalseAndExpiresAtBefore(String phoneNumber, OffsetDateTime now);
}
