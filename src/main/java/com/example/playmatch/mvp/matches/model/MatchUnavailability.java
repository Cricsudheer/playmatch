package com.example.playmatch.mvp.matches.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "match_unavailability",
        uniqueConstraints = @UniqueConstraint(name = "uq_match_unavailable_user", columnNames = {"match_id", "user_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchUnavailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_id", nullable = false)
    private UUID matchId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
