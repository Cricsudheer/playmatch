package com.example.playmatch.mvp.matches.dto;

import com.example.playmatch.mvp.matches.model.MatchStatus;
import com.example.playmatch.mvp.matches.model.EventType;
import com.example.playmatch.mvp.matches.model.BallCategory;
import com.example.playmatch.mvp.matches.model.BallVariant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for My Games endpoint
 * Returns summary of matches where user is captain or participant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyGamesResponseDto {
    private List<GameSummaryDto> games;
    private int totalCount;
    private int upcomingCount;
    private int completedCount;
    private int cancelledCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameSummaryDto {
        private UUID matchId;
        private String teamName;
        private EventType eventType;
        private BallCategory ballCategory;
        private BallVariant ballVariant;
        private Integer overs;
        private MatchStatus status;
        private OffsetDateTime startTime;
        private String groundMapsUrl;
        private Double groundLat;
        private Double groundLng;
        private Integer feePerPerson;
        private Integer emergencyFee;

        // User's relationship to this match
        private String userRole;  // "CAPTAIN", "TEAM", "BACKUP", "EMERGENCY"
        private Boolean isCaptain;

        // Participant counts
        private Integer teamCount;
        private Integer backupCount;
        private Integer emergencyCount;
        private Integer requiredPlayers;
        private Integer backupSlots;

        // User's payment status (if participant)
        private String paymentStatus;  // "PAID", "UNPAID", null if captain only
        private String paymentMode;    // "CASH", "UPI", null
        private Integer feeAmount;     // User's fee amount
    }
}
