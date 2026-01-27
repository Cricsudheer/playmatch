package com.example.playmatch.mvp.payments.service;

import com.example.playmatch.mvp.matches.model.PaymentMode;
import com.example.playmatch.mvp.matches.model.PaymentStatus;
import com.example.playmatch.mvp.payments.dto.PaymentTrackingDto;

import java.util.UUID;

public interface PaymentService {
    /**
     * Mark payment for a participant (captain only)
     *
     * @param matchId     Match ID
     * @param userId      User who paid
     * @param paymentMode Payment mode (CASH or UPI)
     * @param captainId   Captain marking the payment
     */
    void markPayment(UUID matchId, Long userId, PaymentMode paymentMode, Long captainId);

    /**
     * Get payment tracking details for a match
     *
     * @param matchId       Match ID
     * @param requestUserId User requesting the data
     * @param filterStatus  Optional filter by payment status (PAID/UNPAID)
     * @return Payment tracking data (captain sees all, players see only their own)
     */
    PaymentTrackingDto getPaymentTracking(UUID matchId, Long requestUserId, PaymentStatus filterStatus);
}
