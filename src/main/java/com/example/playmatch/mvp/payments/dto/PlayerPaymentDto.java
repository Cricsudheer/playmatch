package com.example.playmatch.mvp.payments.dto;

import com.example.playmatch.mvp.matches.model.PaymentMode;
import com.example.playmatch.mvp.matches.model.PaymentStatus;
import com.example.playmatch.mvp.matches.model.ParticipantRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerPaymentDto {
    private Long userId;
    private String playerName;
    private String phoneNumber;
    private ParticipantRole role;
    private Integer feeAmount;
    private PaymentStatus paymentStatus;
    private PaymentMode paymentMode;
    private OffsetDateTime paidAt;
}
