package com.example.playmatch.mvp.matches.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchRespondDto {
    @NotNull(message = "Response is required")
    @Pattern(regexp = "^(YES|NO)$", message = "Response must be either YES or NO")
    private String response;
}
