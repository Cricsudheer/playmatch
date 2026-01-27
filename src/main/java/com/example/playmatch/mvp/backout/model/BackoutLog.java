package com.example.playmatch.mvp.backout.model;

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
@Table(name = "backout_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackoutLog {
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
    @Column(name = "reason", nullable = false, length = 30)
    private BackoutReason reason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "logged_by", nullable = false)
    private Long loggedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logged_by", insertable = false, updatable = false)
    private MvpUser captain;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
