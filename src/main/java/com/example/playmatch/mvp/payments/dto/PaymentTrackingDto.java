package com.example.playmatch.mvp.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTrackingDto {
    private Integer totalPlayers;
    private Integer paidCount;
    private Integer unpaidCount;
    private Integer totalCollected;
    private Integer totalPending;
    private List<PlayerPaymentDto> players;
}
