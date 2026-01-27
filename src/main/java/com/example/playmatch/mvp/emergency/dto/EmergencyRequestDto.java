package com.example.playmatch.mvp.emergency.dto;

import com.example.playmatch.mvp.emergency.model.EmergencyRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyRequestDto {
    private Long requestId;
    private Long userId;
    private String userName;
    private String phoneNumber;
    private String area;
    private Integer trustScore;
    private EmergencyRequestStatus status;
    private OffsetDateTime requestedAt;
    private OffsetDateTime lockExpiresAt;
}
