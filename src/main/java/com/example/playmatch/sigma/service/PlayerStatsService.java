package com.example.playmatch.sigma.service;

import com.example.playmatch.sigma.model.Player;
import com.example.playmatch.sigma.model.BattingStats;
import com.example.playmatch.sigma.model.BowlingStats;
import com.example.playmatch.sigma.model.DismissalStats;
import com.example.playmatch.sigma.repository.PlayerRepository;
import com.example.playmatch.sigma.repository.BattingStatsRepository;
import com.example.playmatch.sigma.repository.BowlingStatsRepository;
import com.example.playmatch.sigma.repository.DismissalStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerStatsService {

    private final PlayerRepository playerRepository;
    private final BattingStatsRepository battingStatsRepository;
    private final BowlingStatsRepository bowlingStatsRepository;
    private final DismissalStatsRepository dismissalStatsRepository;

    /**
     * Get all stats (batting, bowling, dismissal) for a player by playerId
     */
    public PlayerStatsDto getPlayerStats(Long playerId) {
        log.info("Fetching stats for playerId={}", playerId);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with id: " + playerId));

        List<BattingStats> battingStats = battingStatsRepository.findByPlayer_Id(playerId);
        List<BowlingStats> bowlingStats = bowlingStatsRepository.findByPlayer_Id(playerId);
        List<DismissalStats> dismissalStats = dismissalStatsRepository.findByPlayer_Id(playerId);

        return PlayerStatsDto.builder()
                .playerId(player.getId())
                .playerName(player.getName())
                .battingStats(battingStats)
                .bowlingStats(bowlingStats)
                .dismissalStats(dismissalStats)
                .build();
    }

    /**
     * Get player information by playerId
     */
    public PlayerInfoDto getPlayerInformation(Long playerId) {
        log.info("Fetching player information for playerId={}", playerId);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with id: " + playerId));

        return PlayerInfoDto.builder()
                .id(player.getId())
                .name(player.getName())
                .build();
    }

    /**
     * Get stats for all players with optimized inner joins
     * Fetches batting, bowling, and dismissal stats for all players in a single optimized query
     */
    public List<AllPlayersStatsDto> getAllPlayersStats() {
        log.info("Fetching stats for all players with optimized inner joins");

        List<BattingStats> battingStatsList = battingStatsRepository.findAllWithPlayerOptimized();
        List<BowlingStats> bowlingStatsList = bowlingStatsRepository.findAllWithPlayerOptimized();
        List<DismissalStats> dismissalStatsList = dismissalStatsRepository.findAllWithPlayerOptimized();

        // Create a map for quick lookup by player ID
        java.util.Map<Long, AllPlayersStatsDto> statsMap = new java.util.LinkedHashMap<>();

        // Add batting stats
        for (BattingStats bs : battingStatsList) {
            Long playerId = bs.getPlayer().getId();
            statsMap.computeIfAbsent(playerId, pid -> AllPlayersStatsDto.builder()
                    .playerId(pid)
                    .playerName(bs.getPlayer().getName())
                    .build())
                    .setBattingStats(bs);
        }

        // Add bowling stats
        for (BowlingStats bs : bowlingStatsList) {
            Long playerId = bs.getPlayer().getId();
            statsMap.computeIfAbsent(playerId, pid -> AllPlayersStatsDto.builder()
                    .playerId(pid)
                    .playerName(bs.getPlayer().getName())
                    .build())
                    .setBowlingStats(bs);
        }

        // Add dismissal stats
        for (DismissalStats ds : dismissalStatsList) {
            Long playerId = ds.getPlayer().getId();
            statsMap.computeIfAbsent(playerId, pid -> AllPlayersStatsDto.builder()
                    .playerId(pid)
                    .playerName(ds.getPlayer().getName())
                    .build())
                    .setDismissalStats(ds);
        }

        log.info("Successfully fetched stats for {} players", statsMap.size());
        return new java.util.ArrayList<>(statsMap.values());
    }
}