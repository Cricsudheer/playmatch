package com.example.playmatch.mvp.matches.service;

import com.example.playmatch.mvp.matches.dto.CreateMatchDto;
import com.example.playmatch.mvp.matches.dto.MatchCreatedResponseDto;
import com.example.playmatch.mvp.matches.dto.MatchResponseDto;
import com.example.playmatch.mvp.matches.dto.MyGamesResponseDto;

import java.util.UUID;

public interface MatchService {
    /**
     * Create a new match
     *
     * @param dto         Match creation data
     * @param captainId   ID of user creating the match
     * @return Match created response with invite URLs
     */
    MatchCreatedResponseDto createMatch(CreateMatchDto dto, Long captainId);

    /**
     * Get match details (role-aware view)
     *
     * @param matchId Match ID
     * @param userId  Current user ID (can be null for public view)
     * @return Match details
     */
    MatchResponseDto getMatch(UUID matchId, Long userId);

    /**
     * Respond YES to match invitation
     *
     * @param matchId Match ID
     * @param userId  User ID responding
     */
    void respondYes(UUID matchId, Long userId);

    /**
     * Respond NO to match invitation
     *
     * @param matchId Match ID
     * @param userId  User ID responding
     */
    void respondNo(UUID matchId, Long userId);

    /**
     * Mark match as completed (captain only)
     *
     * @param matchId   Match ID
     * @param captainId Captain user ID
     */
    void completeMatch(UUID matchId, Long captainId);

    /**
     * Cancel match (captain only)
     *
     * @param matchId   Match ID
     * @param captainId Captain user ID
     */
    void cancelMatch(UUID matchId, Long captainId);

    /**
     * Get all matches where user is captain or participant
     *
     * @param userId Current user ID
     * @return List of user's games with summary info
     */
    MyGamesResponseDto getMyGames(Long userId);
}
