package com.example.playmatch.mvp.emergency.model;

import com.example.playmatch.mvp.matches.model.Match;
import com.example.playmatch.mvp.users.model.MvpUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "emergency_request")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_id", nullable = false)
    private UUID matchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", insertable = false, updatable = false)
    private Match match;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private MvpUser user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EmergencyRequestStatus status = EmergencyRequestStatus.REQUESTED;

    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime requestedAt;

    @Column(name = "lock_expires_at", nullable = false)
    private OffsetDateTime lockExpiresAt;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "rejected_at")
    private OffsetDateTime rejectedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
        if (requestedAt == null) {
            requestedAt = createdAt;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public boolean isExpired() {
        return status == EmergencyRequestStatus.REQUESTED &&
               OffsetDateTime.now().isAfter(lockExpiresAt);
    }
}
