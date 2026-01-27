package com.example.playmatch.mvp.invites.controller;

import com.example.playmatch.mvp.invites.dto.InviteResponseDto;
import com.example.playmatch.mvp.invites.model.MatchInvite;
import com.example.playmatch.mvp.invites.service.InviteService;
import com.example.playmatch.mvp.matches.model.Match;
import com.example.playmatch.mvp.matches.repository.MatchRepository;
import com.example.playmatch.mvp.common.error.MvpError;
import com.example.playmatch.mvp.common.exception.MvpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/mvp/invites")
@RequiredArgsConstructor
@Slf4j
public class InviteController {

    private final InviteService inviteService;
    private final MatchRepository matchRepository;

    @GetMapping("/{token}")
    public ResponseEntity<InviteResponseDto> resolveInvite(@PathVariable String token) {
        log.info("Resolving invite token: {}", token);

        // Validate token is not null or empty
        if (token == null || token.trim().isEmpty()) {
            throw new MvpException(MvpError.INVITE_NOT_FOUND);
        }

        MatchInvite invite = inviteService.resolveInvite(token);

        // Validate invite has matchId
        if (invite.getMatchId() == null) {
            log.error("Invite has null matchId: token={}", token);
            throw new MvpException(MvpError.MATCH_NOT_FOUND);
        }

        Match match = matchRepository.findById(invite.getMatchId())
            .orElseThrow(() -> new MvpException(MvpError.MATCH_NOT_FOUND));

        InviteResponseDto.InviteResponseDtoBuilder responseBuilder = InviteResponseDto.builder()
            .matchId(match.getId())
            .inviteType(invite.getType())
            .teamName(match.getTeamName() != null ? match.getTeamName() : "Unknown Team")
            .eventType(match.getEventType())
            .ballCategory(match.getBallCategory())
            .ballVariant(match.getBallVariant())
            .overs(match.getOvers())
            .groundMapsUrl(match.getGroundMapsUrl())
            .startTime(match.getStartTime())
            .requiresAuth(false); // Invite resolution doesn't require auth

        if (invite.getType() == com.example.playmatch.mvp.invites.model.InviteType.TEAM) {
            responseBuilder.matchFees(match.getFeePerPerson() != null ? match.getFeePerPerson() : 0);
        }

        return ResponseEntity.ok(responseBuilder.build());
    }
}
