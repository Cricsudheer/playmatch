package com.example.playmatch.mvp.invites.service.impl;

import com.example.playmatch.mvp.common.error.MvpError;
import com.example.playmatch.mvp.common.exception.MvpException;
import com.example.playmatch.mvp.invites.model.InviteType;
import com.example.playmatch.mvp.invites.model.MatchInvite;
import com.example.playmatch.mvp.invites.repository.MatchInviteRepository;
import com.example.playmatch.mvp.invites.service.InviteService;
import com.example.playmatch.mvp.invites.service.InviteTokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InviteServiceImpl implements InviteService {

    private final MatchInviteRepository matchInviteRepository;
    private final InviteTokenGenerator inviteTokenGenerator;

    @Value("${app.mvp.invite-base-url}")
    private String inviteBaseUrl;

    @Override
    @Transactional
    public MatchInvite createInvite(UUID matchId, InviteType inviteType) {
        // Generate unique token
        String token = inviteTokenGenerator.generate();

        MatchInvite invite = MatchInvite.builder()
            .inviteToken(token)
            .matchId(matchId)
            .type(inviteType)
            .expiresAt(null) // No expiry for MVP
            .build();

        MatchInvite saved = matchInviteRepository.save(invite);
        log.info("Created {} invite for match {}: token={}", inviteType, matchId, token);

        return saved;
    }

    @Override
    public MatchInvite resolveInvite(String token) {
        MatchInvite invite = matchInviteRepository.findByInviteToken(token)
            .orElseThrow(() -> new MvpException(MvpError.INVITE_NOT_FOUND));

        // Check expiry
        if (invite.isExpired()) {
            throw new MvpException(MvpError.INVITE_EXPIRED);
        }

        log.debug("Resolved invite token: {} -> matchId: {}, type: {}",
            token, invite.getMatchId(), invite.getType());

        return invite;
    }

    @Override
    public String buildInviteUrl(String token) {
        return inviteBaseUrl + "/" + token;
    }
}
