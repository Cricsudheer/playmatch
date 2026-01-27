package com.example.playmatch.mvp.invites.model;

import com.example.playmatch.mvp.matches.model.Match;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "match_invite",
        indexes = @Index(name = "idx_invite_token", columnList = "invite_token", unique = true))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchInvite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invite_token", nullable = false, unique = true, length = 8)
    private String inviteToken;

    @Column(name = "match_id", nullable = false)
    private UUID matchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", insertable = false, updatable = false)
    private Match match;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private InviteType type;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public boolean isExpired() {
        return expiresAt != null && OffsetDateTime.now().isAfter(expiresAt);
    }
}
