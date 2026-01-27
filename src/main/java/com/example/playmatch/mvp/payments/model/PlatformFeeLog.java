package com.example.playmatch.mvp.payments.model;

import com.example.playmatch.mvp.matches.model.Match;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "platform_fee_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformFeeLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_id", nullable = false)
    private UUID matchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", insertable = false, updatable = false)
    private Match match;

    @Column(name = "amount", nullable = false)
    @Builder.Default
    private Integer amount = 50;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "RECORDED";

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
