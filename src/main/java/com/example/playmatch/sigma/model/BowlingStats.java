package com.example.playmatch.sigma.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bowling_stats")
public class BowlingStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "innings", nullable = false)
    private Integer innings;

    @Column(name = "wickets_taken", nullable = false)
    private Integer wicketsTaken;

    @Column(name = "economy_rate", precision = 5, scale = 2)
    private BigDecimal economyRate;


}

