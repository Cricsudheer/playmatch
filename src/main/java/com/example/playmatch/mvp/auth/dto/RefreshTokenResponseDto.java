package com.example.playmatch.mvp.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenResponseDto {

    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String phoneNumber;
    private String name;
}

