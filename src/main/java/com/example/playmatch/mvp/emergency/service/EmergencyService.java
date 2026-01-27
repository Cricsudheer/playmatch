package com.example.playmatch.mvp.emergency.service;

import com.example.playmatch.mvp.emergency.dto.EmergencyRequestDto;

import java.util.List;
import java.util.UUID;

public interface EmergencyService {
    /**
     * Request emergency spot in a match
     *
     * @param matchId Match ID
     * @param userId  User requesting
     */
    void requestEmergencySpot(UUID matchId, Long userId);

    /**
     * Get pending emergency requests for a match (captain only)
     *
     * @param matchId Match ID
     * @return List of pending requests
     */
    List<EmergencyRequestDto> getPendingRequests(UUID matchId);

    /**
     * Approve emergency request (captain only)
     *
     * @param matchId   Match ID
     * @param requestId Request ID
     * @param captainId Captain user ID
     */
    void approveRequest(UUID matchId, Long requestId, Long captainId);

    /**
     * Reject emergency request (captain only)
     *
     * @param matchId   Match ID
     * @param requestId Request ID
     * @param captainId Captain user ID
     */
    void rejectRequest(UUID matchId, Long requestId, Long captainId);
}
