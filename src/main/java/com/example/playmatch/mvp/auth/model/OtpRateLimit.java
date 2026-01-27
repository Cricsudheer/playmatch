package com.example.playmatch.mvp.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "otp_rate_limit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpRateLimit {
    @Id
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "request_count", nullable = false)
    @Builder.Default
    private Integer requestCount = 0;

    @Column(name = "window_start", nullable = false)
    private OffsetDateTime windowStart;

    public void incrementCount() {
        this.requestCount++;
    }

    public void resetWindow() {
        this.requestCount = 1;
        this.windowStart = OffsetDateTime.now();
    }

    public boolean isWithinWindow(int windowMinutes) {
        return OffsetDateTime.now().isBefore(windowStart.plusMinutes(windowMinutes));
    }
}
