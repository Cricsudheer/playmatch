package com.example.playmatch.sigma.service;

import com.example.playmatch.sigma.model.BattingStats;
import com.example.playmatch.sigma.model.BowlingStats;
import com.example.playmatch.sigma.model.DismissalStats;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllPlayersStatsDto {
    private Long playerId;
    private String playerName;
    private BattingStats battingStats;
    private BowlingStats bowlingStats;
    private DismissalStats dismissalStats;
}

