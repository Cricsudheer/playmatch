package com.example.playmatch.mvp.matches.dto;

import com.example.playmatch.mvp.matches.model.BallCategory;
import com.example.playmatch.mvp.matches.model.BallVariant;
import com.example.playmatch.mvp.matches.model.EventType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMatchDto {
    @NotBlank(message = "Team name is required")
    @Size(min = 2, max = 100, message = "Team name must be between 2 and 100 characters")
    private String teamName;

    @NotNull(message = "Event type is required")
    private EventType eventType;

    @NotNull(message = "Ball category is required")
    private BallCategory ballCategory;

    @NotNull(message = "Ball variant is required")
    private BallVariant ballVariant;

    @NotBlank(message = "Ground maps URL is required")
    private String groundMapsUrl;

    @NotNull(message = "Overs is required")
    @Min(value = 1, message = "Overs must be at least 1")
    @Max(value = 50, message = "Overs must not exceed 50")
    private Integer overs;

    @NotNull(message = "Fee per person is required")
    @Min(value = 0, message = "Fee must be non-negative")
    private Integer feePerPerson;

    @Min(value = 0, message = "Emergency fee must be non-negative")
    private Integer emergencyFee;

    @Min(value = 1, message = "Required players must be at least 1")
    @Builder.Default
    private Integer requiredPlayers = 11;

    @Min(value = 0, message = "Backup slots must be non-negative")
    @Builder.Default
    private Integer backupSlots = 2;

    @Builder.Default
    private Boolean emergencyEnabled = false;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private OffsetDateTime startTime;
}
