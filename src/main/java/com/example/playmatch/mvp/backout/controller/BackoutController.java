package com.example.playmatch.mvp.backout.controller;

import com.example.playmatch.mvp.auth.security.CurrentMvpUser;
import com.example.playmatch.mvp.backout.dto.LogBackoutDto;
import com.example.playmatch.mvp.backout.service.BackoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v2/mvp/matches/{matchId}/backout")
@RequiredArgsConstructor
@Slf4j
public class BackoutController {

    private final BackoutService backoutService;

    @PostMapping
    public ResponseEntity<Void> logBackout(
        @PathVariable UUID matchId,
        @Valid @RequestBody LogBackoutDto dto
    ) {
        Long captainId = CurrentMvpUser.getUserId();
        log.info("Log backout: matchId={}, userId={}, reason={}, captainId={}",
            matchId, dto.getUserId(), dto.getReason(), captainId);

        backoutService.logBackout(matchId, dto.getUserId(), dto.getReason(), dto.getNotes(), captainId);
        return ResponseEntity.noContent().build();
    }
}
