package com.example.playmatch.mvp.matches.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchCreatedResponseDto {
    private UUID matchId;
    private String teamInviteUrl;
    private String emergencyInviteUrl;
}
