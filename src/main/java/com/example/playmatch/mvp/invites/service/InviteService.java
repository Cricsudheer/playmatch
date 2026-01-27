package com.example.playmatch.mvp.invites.service;

import com.example.playmatch.mvp.invites.model.InviteType;
import com.example.playmatch.mvp.invites.model.MatchInvite;

import java.util.UUID;

public interface InviteService {
    /**
     * Create invite link for a match
     *
     * @param matchId    Match ID
     * @param inviteType Type of invite (TEAM or EMERGENCY)
     * @return Created match invite
     */
    MatchInvite createInvite(UUID matchId, InviteType inviteType);

    /**
     * Resolve invite by token
     *
     * @param token Invite token
     * @return Match invite details
     */
    MatchInvite resolveInvite(String token);

    /**
     * Build full invite URL
     *
     * @param token Invite token
     * @return Full invite URL
     */
    String buildInviteUrl(String token);
}
