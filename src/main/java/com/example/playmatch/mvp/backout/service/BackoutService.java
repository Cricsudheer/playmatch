package com.example.playmatch.mvp.backout.service;

import com.example.playmatch.mvp.backout.model.BackoutReason;

import java.util.UUID;

public interface BackoutService {
    /**
     * Log a backout for a participant (captain only)
     *
     * @param matchId   Match ID
     * @param userId    User who backed out
     * @param reason    Reason for backout
     * @param notes     Additional notes
     * @param captainId Captain logging the backout
     */
    void logBackout(UUID matchId, Long userId, BackoutReason reason, String notes, Long captainId);
}
