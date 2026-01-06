package com.example.playmatch.sigma.controller;

import com.example.playmatch.sigma.service.PlayerInfoDto;
import com.example.playmatch.sigma.service.PlayerStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("sigma/api/players")
@RequiredArgsConstructor
public class PlayerInfoController {

    private final PlayerStatsService playerStatsService;

    /**
     * Get player information by playerId
     *
     * @param playerId the ID of the player
     * @return PlayerInfoDto containing player details or 404 if player not found
     */
    @GetMapping("/{playerId}")
    public ResponseEntity<PlayerInfoDto> getPlayerInformation(@PathVariable Long playerId) {
        log.info("Fetching player information for playerId={}", playerId);
        try {
            PlayerInfoDto playerInfo = playerStatsService.getPlayerInformation(playerId);
            return ResponseEntity.ok(playerInfo);
        } catch (IllegalArgumentException notFound) {
            log.warn("Player not found with id={}", playerId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            log.error("Error fetching player information for playerId={}", playerId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}


