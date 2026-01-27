package com.example.playmatch.mvp.backout.dto;

import com.example.playmatch.mvp.backout.model.BackoutReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogBackoutDto {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Reason is required")
    private BackoutReason reason;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
