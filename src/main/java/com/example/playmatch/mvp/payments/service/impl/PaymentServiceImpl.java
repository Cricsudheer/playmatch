package com.example.playmatch.mvp.payments.service.impl;

import com.example.playmatch.mvp.common.error.MvpError;
import com.example.playmatch.mvp.common.exception.MvpException;
import com.example.playmatch.mvp.matches.model.Match;
import com.example.playmatch.mvp.matches.model.MatchParticipant;
import com.example.playmatch.mvp.matches.model.PaymentMode;
import com.example.playmatch.mvp.matches.model.PaymentStatus;
import com.example.playmatch.mvp.matches.repository.MatchParticipantRepository;
import com.example.playmatch.mvp.matches.repository.MatchRepository;
import com.example.playmatch.mvp.payments.dto.PaymentTrackingDto;
import com.example.playmatch.mvp.payments.dto.PlayerPaymentDto;
import com.example.playmatch.mvp.payments.service.PaymentService;
import com.example.playmatch.mvp.users.model.MvpUser;
import com.example.playmatch.mvp.users.repository.MvpUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final MatchRepository matchRepository;
    private final MatchParticipantRepository participantRepository;
    private final MvpUserRepository mvpUserRepository;

    @Override
    @Transactional
    public void markPayment(UUID matchId, Long userId, PaymentMode paymentMode, Long captainId) {
        // Validate input parameters
        if (matchId == null) {
            throw new MvpException(MvpError.MATCH_NOT_FOUND);
        }
        if (userId == null) {
            throw new MvpException(MvpError.PARTICIPANT_NOT_FOUND);
        }
        if (captainId == null) {
            throw new MvpException(MvpError.NOT_CAPTAIN);
        }

        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new MvpException(MvpError.MATCH_NOT_FOUND));

        // Verify captain
        if (!match.isCaptain(captainId)) {
            throw new MvpException(MvpError.NOT_CAPTAIN);
        }

        // Find participant
        MatchParticipant participant = participantRepository.findByMatchIdAndUserId(matchId, userId)
            .orElseThrow(() -> new MvpException(MvpError.PARTICIPANT_NOT_FOUND));

        // Prevent marking already paid payments (idempotency check with warning)
        if (participant.getPaymentStatus() == PaymentStatus.PAID) {
            log.warn("Payment already marked as PAID: matchId={}, userId={}, currentMode={}",
                matchId, userId, participant.getPaymentMode());
            // Allow updating payment mode even if already paid
        }

        // Mark as paid
        participant.setPaymentStatus(PaymentStatus.PAID);
        participant.setPaymentMode(paymentMode);
        participantRepository.save(participant);

        log.info("Payment marked: matchId={}, userId={}, mode={}, markedBy={}",
            matchId, userId, paymentMode, captainId);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentTrackingDto getPaymentTracking(UUID matchId, Long requestUserId, PaymentStatus filterStatus) {
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new MvpException(MvpError.MATCH_NOT_FOUND));

        boolean isCaptain = match.isCaptain(requestUserId);

        // Verify user has access to this match (either captain or participant)
        if (!isCaptain) {
            Optional<MatchParticipant> requestingUser = participantRepository.findByMatchIdAndUserId(matchId, requestUserId);
            if (requestingUser.isEmpty()) {
                throw new MvpException(MvpError.PARTICIPANT_NOT_FOUND);
            }
        }

        // Get all participants with user data eagerly loaded to avoid N+1 queries
        List<MatchParticipant> participants = participantRepository.findByMatchIdWithUser(matchId);

        // Handle empty participants list (no one has joined yet)
        if (participants.isEmpty()) {
            log.warn("No participants found for match: {}", matchId);
            return PaymentTrackingDto.builder()
                .totalPlayers(0)
                .paidCount(0)
                .unpaidCount(0)
                .totalCollected(0)
                .totalPending(0)
                .players(new ArrayList<>())
                .build();
        }

        // Apply filter if provided
        if (filterStatus != null) {
            participants = participants.stream()
                .filter(p -> p.getPaymentStatus() == filterStatus)
                .collect(Collectors.toList());
        }

        // Build player payment DTOs based on access level
        List<PlayerPaymentDto> playerPayments;
        if (isCaptain) {
            // Captain sees all players
            playerPayments = participants.stream()
                .map(this::mapToPlayerPaymentDto)
                .collect(Collectors.toList());
        } else {
            // Regular player sees only their own payment info
            playerPayments = participants.stream()
                .filter(p -> p.getUserId().equals(requestUserId))
                .map(this::mapToPlayerPaymentDto)
                .collect(Collectors.toList());
        }

        // Calculate summary stats (based on all participants for captain, only self for player)
        List<MatchParticipant> statsBase = isCaptain ? participants :
            participants.stream().filter(p -> p.getUserId().equals(requestUserId)).collect(Collectors.toList());

        int totalPlayers = statsBase.size();
        int paidCount = (int) statsBase.stream().filter(p -> p.getPaymentStatus() == PaymentStatus.PAID).count();
        int unpaidCount = totalPlayers - paidCount;
        int totalCollected = statsBase.stream()
            .filter(p -> p.getPaymentStatus() == PaymentStatus.PAID)
            .mapToInt(MatchParticipant::getFeeAmount)
            .sum();
        int totalPending = statsBase.stream()
            .filter(p -> p.getPaymentStatus() == PaymentStatus.UNPAID)
            .mapToInt(MatchParticipant::getFeeAmount)
            .sum();

        return PaymentTrackingDto.builder()
            .totalPlayers(totalPlayers)
            .paidCount(paidCount)
            .unpaidCount(unpaidCount)
            .totalCollected(totalCollected)
            .totalPending(totalPending)
            .players(playerPayments)
            .build();
    }

    private PlayerPaymentDto mapToPlayerPaymentDto(MatchParticipant participant) {
        // Try to use eagerly loaded user first, fallback to fetching if needed
        MvpUser user = participant.getUser();

        // Fallback: fetch user if not eagerly loaded (shouldn't happen with findByMatchIdWithUser)
        if (user == null) {
            user = mvpUserRepository.findById(participant.getUserId()).orElse(null);
        }

        String playerName = "Unknown";
        String phoneNumber = null;

        if (user != null) {
            playerName = user.getName() != null ? user.getName() : "Unknown";
            phoneNumber = user.getPhoneNumber();
        } else {
            log.warn("User not found for participant: userId={}, matchId={}",
                participant.getUserId(), participant.getMatchId());
        }

        return PlayerPaymentDto.builder()
            .userId(participant.getUserId())
            .playerName(playerName)
            .phoneNumber(phoneNumber)
            .role(participant.getRole())
            .feeAmount(participant.getFeeAmount())
            .paymentStatus(participant.getPaymentStatus())
            .paymentMode(participant.getPaymentMode())
            .paidAt(participant.getPaymentStatus() == PaymentStatus.PAID ? participant.getUpdatedAt() : null)
            .build();
    }
}
