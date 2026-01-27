package com.example.playmatch.mvp.invites.dto;

import com.example.playmatch.mvp.invites.model.InviteType;
import com.example.playmatch.mvp.matches.model.EventType;
import com.example.playmatch.mvp.matches.model.BallCategory;
import com.example.playmatch.mvp.matches.model.BallVariant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteResponseDto {
    private UUID matchId;
    private InviteType inviteType;
    private String teamName;
    private EventType eventType;
    private BallCategory ballCategory;
    private BallVariant ballVariant;
    private Integer overs;
    private String groundMapsUrl;
    private OffsetDateTime startTime;
    private Boolean requiresAuth;
    private Integer matchFees;
}
