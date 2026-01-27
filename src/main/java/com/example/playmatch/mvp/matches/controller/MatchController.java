package com.example.playmatch.mvp.matches.controller;

import com.example.playmatch.mvp.auth.security.CurrentMvpUser;
import com.example.playmatch.mvp.matches.dto.CreateMatchDto;
import com.example.playmatch.mvp.matches.dto.MatchCreatedResponseDto;
import com.example.playmatch.mvp.matches.dto.MatchRespondDto;
import com.example.playmatch.mvp.matches.dto.MatchResponseDto;
import com.example.playmatch.mvp.matches.dto.MyGamesResponseDto;
import com.example.playmatch.mvp.matches.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v2/mvp/matches")
@RequiredArgsConstructor
@Slf4j
public class MatchController {

    private final MatchService matchService;

    @PostMapping
    public ResponseEntity<MatchCreatedResponseDto> createMatch(@Valid @RequestBody CreateMatchDto dto) {
        Long captainId = CurrentMvpUser.getUserId();
        log.info("Create match request from captain: {}", captainId);

        MatchCreatedResponseDto response = matchService.createMatch(dto, captainId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-games")
    public ResponseEntity<MyGamesResponseDto> getMyGames() {
        Long userId = CurrentMvpUser.getUserId();
        log.info("Get my games request: userId={}", userId);

        MyGamesResponseDto response = matchService.getMyGames(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchResponseDto> getMatch(@PathVariable UUID id) {
        // Try to get current user ID, can be null for public view
        Long userId;
        try {
            userId = CurrentMvpUser.getUserId();
        } catch (Exception e) {
            userId = null; // Public view
        }

        log.info("Get match request: matchId={}, userId={}", id, userId);
        MatchResponseDto response = matchService.getMatch(id, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/respond")
    public ResponseEntity<Void> respondToMatch(
        @PathVariable UUID id,
        @Valid @RequestBody MatchRespondDto dto
    ) {
        Long userId = CurrentMvpUser.getUserId();
        log.info("Match response: matchId={}, userId={}, response={}", id, userId, dto.getResponse());

        if ("YES".equals(dto.getResponse())) {
            matchService.respondYes(id, userId);
        } else {
            matchService.respondNo(id, userId);
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completeMatch(@PathVariable UUID id) {
        Long captainId = CurrentMvpUser.getUserId();
        log.info("Complete match request: matchId={}, captainId={}", id, captainId);

        matchService.completeMatch(id, captainId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelMatch(@PathVariable UUID id) {
        Long captainId = CurrentMvpUser.getUserId();
        log.info("Cancel match request: matchId={}, captainId={}", id, captainId);

        matchService.cancelMatch(id, captainId);
        return ResponseEntity.noContent().build();
    }
}
