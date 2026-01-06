package com.example.playmatch.sigma.service;

import com.example.playmatch.sigma.model.BattingStats;
import com.example.playmatch.sigma.model.BowlingStats;
import com.example.playmatch.sigma.model.DismissalStats;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStatsDto {
    private Long playerId;
    private String playerName;
    private List<BattingStats> battingStats;
    private List<BowlingStats> bowlingStats;
    private List<DismissalStats> dismissalStats;
}

