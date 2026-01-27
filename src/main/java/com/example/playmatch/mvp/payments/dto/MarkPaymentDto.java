package com.example.playmatch.mvp.payments.dto;

import com.example.playmatch.mvp.matches.model.PaymentMode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkPaymentDto {
    @NotNull(message = "User ID is required")
    private Long userId;

    private PaymentMode paymentMode;
}
