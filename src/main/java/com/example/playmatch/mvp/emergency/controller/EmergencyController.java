package com.example.playmatch.mvp.emergency.controller;

import com.example.playmatch.mvp.auth.security.CurrentMvpUser;
import com.example.playmatch.mvp.emergency.dto.EmergencyRequestDto;
import com.example.playmatch.mvp.emergency.service.EmergencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v2/mvp/matches/{matchId}/emergency")
@RequiredArgsConstructor
@Slf4j
public class EmergencyController {

    private final EmergencyService emergencyService;

    @PostMapping("/request")
    public ResponseEntity<Void> requestEmergencySpot(@PathVariable UUID matchId) {
        Long userId = CurrentMvpUser.getUserId();
        log.info("Emergency request: matchId={}, userId={}", matchId, userId);

        emergencyService.requestEmergencySpot(matchId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/requests")
    public ResponseEntity<List<EmergencyRequestDto>> getPendingRequests(@PathVariable UUID matchId) {
        Long captainId = CurrentMvpUser.getUserId();
        log.info("Get emergency requests: matchId={}, captainId={}", matchId, captainId);

        List<EmergencyRequestDto> requests = emergencyService.getPendingRequests(matchId);
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/{requestId}/approve")
    public ResponseEntity<Void> approveRequest(
        @PathVariable UUID matchId,
        @PathVariable Long requestId
    ) {
        Long captainId = CurrentMvpUser.getUserId();
        log.info("Approve emergency request: matchId={}, requestId={}, captainId={}",
            matchId, requestId, captainId);

        emergencyService.approveRequest(matchId, requestId, captainId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<Void> rejectRequest(
        @PathVariable UUID matchId,
        @PathVariable Long requestId
    ) {
        Long captainId = CurrentMvpUser.getUserId();
        log.info("Reject emergency request: matchId={}, requestId={}, captainId={}",
            matchId, requestId, captainId);

        emergencyService.rejectRequest(matchId, requestId, captainId);
        return ResponseEntity.noContent().build();
    }
}
