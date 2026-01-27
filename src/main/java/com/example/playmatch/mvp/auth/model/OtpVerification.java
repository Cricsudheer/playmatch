package com.example.playmatch.mvp.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "otp_verification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "otp_code", nullable = false, length = 255)
    private String otpCode;

    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    @Column(name = "verified", nullable = false)
    @Builder.Default
    private Boolean verified = false;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }
}
