package com.example.playmatch.mvp.matches.dto;

import com.example.playmatch.mvp.matches.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponseDto {
    private UUID matchId;
    private String teamName;
    private EventType eventType;
    private BallCategory ballCategory;
    private BallVariant ballVariant;
    private String groundMapsUrl;
    private Double groundLat;
    private Double groundLng;
    private Integer overs;
    private Integer feePerPerson;
    private Integer emergencyFee;
    private Integer requiredPlayers;
    private Integer backupSlots;
    private Boolean emergencyEnabled;
    private MatchStatus status;
    private OffsetDateTime startTime;
    private OffsetDateTime createdAt;

    // Captain details (if viewing own match)
    private Long captainId;
    private String captainName;
    private String captainPhone;

    // Participant summary (always visible)
    private Integer teamCount;
    private Integer backupCount;
    private Integer emergencyCount;

    // Full participant list (captain-only view)
    private List<ParticipantDto> participants;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantDto {
        private Long userId;
        private String name;
        private String phoneNumber;
        private ParticipantRole role;
        private ParticipantStatus status;
        private Integer feeAmount;
        private PaymentStatus paymentStatus;
        private PaymentMode paymentMode;
    }
}
