package com.example.playmatch.mvp.matches.model;

import com.example.playmatch.mvp.users.model.MvpUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "match")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private MvpUser captain;

    @Column(name = "team_name", nullable = false, length = 100)
    private String teamName;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "ball_category", nullable = false, length = 20)
    private BallCategory ballCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "ball_variant", nullable = false, length = 20)
    private BallVariant ballVariant;

    @Column(name = "ground_maps_url", nullable = false, columnDefinition = "TEXT")
    private String groundMapsUrl;

    @Column(name = "ground_lat")
    private Double groundLat;

    @Column(name = "ground_lng")
    private Double groundLng;

    @Column(name = "overs", nullable = false)
    private Integer overs;

    @Column(name = "fee_per_person", nullable = false)
    private Integer feePerPerson;

    @Column(name = "emergency_fee")
    private Integer emergencyFee;

    @Column(name = "required_players", nullable = false)
    @Builder.Default
    private Integer requiredPlayers = 11;

    @Column(name = "backup_slots", nullable = false)
    @Builder.Default
    private Integer backupSlots = 2;

    @Column(name = "emergency_enabled", nullable = false)
    @Builder.Default
    private Boolean emergencyEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private MatchStatus status = MatchStatus.CREATED;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public boolean isCaptain(Long userId) {
        return createdBy.equals(userId);
    }
}
