package com.example.playmatch.sigma.controller;

import com.example.playmatch.sigma.service.PlayerStatsService;
import com.example.playmatch.sigma.service.PlayerStatsDto;
import com.example.playmatch.sigma.service.AllPlayersStatsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("sigma/api/players")
@RequiredArgsConstructor
public class PlayerStatsController {

    private final PlayerStatsService playerStatsService;

    /**
     * Get stats for a single player
     */
    @GetMapping("/{playerId}/stats")
    public ResponseEntity<PlayerStatsDto> getPlayerStats(@PathVariable Long playerId) {
        log.info("Fetching stats for playerId={}", playerId);
        try {
            PlayerStatsDto stats = playerStatsService.getPlayerStats(playerId);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException notFound) {
            log.warn("Player not found with id={}", playerId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            log.error("Error fetching stats for playerId={}", playerId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get stats for all players with optimized queries
     * Uses inner joins to fetch player names and stats in a single optimized query set
     */
    @GetMapping("/all/stats")
    public ResponseEntity<List<AllPlayersStatsDto>> getAllPlayersStats() {
        log.info("Fetching stats for all players");
        try {
            List<AllPlayersStatsDto> allStats = playerStatsService.getAllPlayersStats();
            log.info("Successfully retrieved stats for {} players", allStats.size());
            return ResponseEntity.ok(allStats);
        } catch (Exception ex) {
            log.error("Error fetching stats for all players", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}