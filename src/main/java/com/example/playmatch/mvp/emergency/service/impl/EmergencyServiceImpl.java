package com.example.playmatch.mvp.emergency.service.impl;

import com.example.playmatch.mvp.common.error.MvpError;
import com.example.playmatch.mvp.common.exception.MvpException;
import com.example.playmatch.mvp.emergency.dto.EmergencyRequestDto;
import com.example.playmatch.mvp.emergency.model.EmergencyPool;
import com.example.playmatch.mvp.emergency.model.EmergencyRequest;
import com.example.playmatch.mvp.emergency.model.EmergencyRequestStatus;
import com.example.playmatch.mvp.emergency.repository.EmergencyPoolRepository;
import com.example.playmatch.mvp.emergency.repository.EmergencyRequestRepository;
import com.example.playmatch.mvp.emergency.service.EmergencyService;
import com.example.playmatch.mvp.matches.model.*;
import com.example.playmatch.mvp.matches.repository.MatchParticipantRepository;
import com.example.playmatch.mvp.matches.repository.MatchRepository;
import com.example.playmatch.mvp.users.model.MvpUser;
import com.example.playmatch.mvp.users.repository.MvpUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmergencyServiceImpl implements EmergencyService {

    private final EmergencyRequestRepository emergencyRequestRepository;
    private final EmergencyPoolRepository emergencyPoolRepository;
    private final MatchRepository matchRepository;
    private final MatchParticipantRepository participantRepository;
    private final MvpUserRepository mvpUserRepository;

    @Value("${app.mvp.emergency.lock-duration-minutes:60}")
    private int lockDurationMinutes;

    @Override
    @Transactional
    public void requestEmergencySpot(UUID matchId, Long userId) {
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new MvpException(MvpError.MATCH_NOT_FOUND));

        // Check if emergency is enabled
        if (!match.getEmergencyEnabled()) {
            throw new MvpException(MvpError.EMERGENCY_NOT_ENABLED);
        }

        // Check if user already has an active request globally
        if (emergencyRequestRepository.existsByUserIdAndStatus(userId, EmergencyRequestStatus.REQUESTED)) {
            throw new MvpException(MvpError.EMERGENCY_ALREADY_REQUESTED);
        }

        // Create emergency request
        EmergencyRequest request = EmergencyRequest.builder()
            .matchId(matchId)
            .userId(userId)
            .status(EmergencyRequestStatus.REQUESTED)
            .requestedAt(OffsetDateTime.now())
            .lockExpiresAt(OffsetDateTime.now().plusMinutes(lockDurationMinutes))
            .build();

        emergencyRequestRepository.save(request);
        log.info("Emergency request created: matchId={}, userId={}, expiresAt={}",
            matchId, userId, request.getLockExpiresAt());
    }

    @Override
    public List<EmergencyRequestDto> getPendingRequests(UUID matchId) {
        List<EmergencyRequest> requests = emergencyRequestRepository
            .findByMatchIdAndStatus(matchId, EmergencyRequestStatus.REQUESTED);

        return requests.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approveRequest(UUID matchId, Long requestId, Long captainId) {
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new MvpException(MvpError.MATCH_NOT_FOUND));

        // Verify captain
        if (!match.isCaptain(captainId)) {
            throw new MvpException(MvpError.NOT_CAPTAIN);
        }

        EmergencyRequest request = emergencyRequestRepository.findById(requestId)
            .orElseThrow(() -> new MvpException(MvpError.EMERGENCY_REQUEST_NOT_FOUND));

        // Verify request belongs to this match
        if (!request.getMatchId().equals(matchId)) {
            throw new MvpException(MvpError.EMERGENCY_REQUEST_NOT_FOUND);
        }

        // Check status
        if (request.getStatus() != EmergencyRequestStatus.REQUESTED) {
            throw new MvpException(MvpError.EMERGENCY_ALREADY_PROCESSED);
        }

        // Check if expired
        if (request.isExpired()) {
            request.setStatus(EmergencyRequestStatus.EXPIRED);
            emergencyRequestRepository.save(request);
            throw new MvpException(MvpError.EMERGENCY_LOCK_EXPIRED);
        }

        // Approve request
        request.setStatus(EmergencyRequestStatus.APPROVED);
        request.setApprovedAt(OffsetDateTime.now());
        emergencyRequestRepository.save(request);

        // Create participant with EMERGENCY role
        Integer feeAmount = match.getEmergencyFee() != null ? match.getEmergencyFee() : match.getFeePerPerson();

        MatchParticipant participant = MatchParticipant.builder()
            .matchId(matchId)
            .userId(request.getUserId())
            .role(ParticipantRole.EMERGENCY)
            .status(ParticipantStatus.CONFIRMED)
            .feeAmount(feeAmount)
            .paymentStatus(PaymentStatus.UNPAID)
            .build();

        participantRepository.save(participant);

        log.info("Emergency request approved: matchId={}, userId={}, requestId={}",
            matchId, request.getUserId(), requestId);
    }

    @Override
    @Transactional
    public void rejectRequest(UUID matchId, Long requestId, Long captainId) {
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new MvpException(MvpError.MATCH_NOT_FOUND));

        // Verify captain
        if (!match.isCaptain(captainId)) {
            throw new MvpException(MvpError.NOT_CAPTAIN);
        }

        EmergencyRequest request = emergencyRequestRepository.findById(requestId)
            .orElseThrow(() -> new MvpException(MvpError.EMERGENCY_REQUEST_NOT_FOUND));

        // Verify request belongs to this match
        if (!request.getMatchId().equals(matchId)) {
            throw new MvpException(MvpError.EMERGENCY_REQUEST_NOT_FOUND);
        }

        // Check status
        if (request.getStatus() != EmergencyRequestStatus.REQUESTED) {
            throw new MvpException(MvpError.EMERGENCY_ALREADY_PROCESSED);
        }

        // Reject request
        request.setStatus(EmergencyRequestStatus.REJECTED);
        request.setRejectedAt(OffsetDateTime.now());
        emergencyRequestRepository.save(request);

        log.info("Emergency request rejected: matchId={}, userId={}, requestId={}",
            matchId, request.getUserId(), requestId);
    }

    private EmergencyRequestDto mapToDto(EmergencyRequest request) {
        MvpUser user = mvpUserRepository.findById(request.getUserId())
            .orElse(null);

        EmergencyPool pool = emergencyPoolRepository.findByUserId(request.getUserId())
            .orElse(null);

        return EmergencyRequestDto.builder()
            .requestId(request.getId())
            .userId(request.getUserId())
            .userName(user != null ? user.getName() : "Unknown")
            .phoneNumber(user != null ? user.getPhoneNumber() : null)
            .area(pool != null ? pool.getArea() : (user != null ? user.getArea() : null))
            .trustScore(pool != null ? pool.getTrustScore() : 0)
            .status(request.getStatus())
            .requestedAt(request.getRequestedAt())
            .lockExpiresAt(request.getLockExpiresAt())
            .build();
    }
}
