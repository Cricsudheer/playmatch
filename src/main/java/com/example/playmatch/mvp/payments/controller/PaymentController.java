package com.example.playmatch.mvp.payments.controller;

import com.example.playmatch.mvp.auth.security.CurrentMvpUser;
import com.example.playmatch.mvp.matches.model.PaymentStatus;
import com.example.playmatch.mvp.payments.dto.MarkPaymentDto;
import com.example.playmatch.mvp.payments.dto.PaymentTrackingDto;
import com.example.playmatch.mvp.payments.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v2/mvp/matches/{matchId}/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/mark")
    public ResponseEntity<Void> markPayment(
        @PathVariable UUID matchId,
        @Valid @RequestBody MarkPaymentDto dto
    ) {
        Long captainId = CurrentMvpUser.getUserId();
        log.info("Mark payment: matchId={}, userId={}, mode={}, captainId={}",
            matchId, dto.getUserId(), dto.getPaymentMode(), captainId);

        paymentService.markPayment(matchId, dto.getUserId(), dto.getPaymentMode(), captainId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tracking")
    public ResponseEntity<PaymentTrackingDto> getPaymentTracking(
        @PathVariable UUID matchId,
        @RequestParam(required = false) PaymentStatus filterStatus
    ) {
        Long userId = CurrentMvpUser.getUserId();
        log.info("Get payment tracking: matchId={}, userId={}, filterStatus={}",
            matchId, userId, filterStatus);

        PaymentTrackingDto tracking = paymentService.getPaymentTracking(matchId, userId, filterStatus);
        return ResponseEntity.ok(tracking);
    }
}
