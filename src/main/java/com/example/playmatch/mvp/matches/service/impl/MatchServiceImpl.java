package com.example.playmatch.mvp.matches.service.impl;

import com.example.playmatch.mvp.common.error.MvpError;
import com.example.playmatch.mvp.common.exception.MvpException;
import com.example.playmatch.mvp.common.util.MapsUrlParser;
import com.example.playmatch.mvp.invites.model.InviteType;
import com.example.playmatch.mvp.invites.model.MatchInvite;
import com.example.playmatch.mvp.invites.service.InviteService;
import com.example.playmatch.mvp.matches.dto.CreateMatchDto;
import com.example.playmatch.mvp.matches.dto.MatchCreatedResponseDto;
import com.example.playmatch.mvp.matches.dto.MatchResponseDto;
import com.example.playmatch.mvp.matches.dto.MyGamesResponseDto;
import com.example.playmatch.mvp.matches.model.*;
import com.example.playmatch.mvp.matches.repository.MatchParticipantRepository;
import com.example.playmatch.mvp.matches.repository.MatchRepository;
import com.example.playmatch.mvp.matches.repository.MatchUnavailabilityRepository;
import com.example.playmatch.mvp.matches.service.MatchService;
import com.example.playmatch.mvp.payments.model.PlatformFeeLog;
import com.example.playmatch.mvp.payments.repository.PlatformFeeLogRepository;
import com.example.playmatch.mvp.users.model.MvpUser;
import com.example.playmatch.mvp.users.repository.MvpUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final MatchParticipantRepository participantRepository;
    private final MatchUnavailabilityRepository unavailabilityRepository;
    private final MvpUserRepository mvpUserRepository;
    private final InviteService inviteService;
    private final PlatformFeeLogRepository platformFeeLogRepository;

    @Value("${app.mvp.platform-fee:50}")
    private Integer platformFee;

    @Override
    @Transactional
    public MatchCreatedResponseDto createMatch(CreateMatchDto dto, Long captainId) {
        // Parse ground coordinates (best-effort)
        Double[] coordinates = MapsUrlParser.parse(dto.getGroundMapsUrl());

        // Create match
        Match match = Match.builder()
            .createdBy(captainId)
            .teamName(dto.getTeamName())
            .eventType(dto.getEventType())
            .ballCategory(dto.getBallCategory())
            .ballVariant(dto.getBallVariant())
            .groundMapsUrl(dto.getGroundMapsUrl())
            .groundLat(coordinates[0])
            .groundLng(coordinates[1])
            .overs(dto.getOvers())
            .feePerPerson(dto.getFeePerPerson())
            .emergencyFee(dto.getEmergencyFee())
            .requiredPlayers(dto.getRequiredPlayers())
            .backupSlots(dto.getBackupSlots())
            .emergencyEnabled(dto.getEmergencyEnabled())
            .status(MatchStatus.CREATED)
            .startTime(dto.getStartTime())
            .build();

        Match savedMatch = matchRepository.save(match);
        log.info("Match created: id={}, captain={}, team={}",
            savedMatch.getId(), captainId, dto.getTeamName());

        // Generate TEAM invite (always)
        MatchInvite teamInvite = inviteService.createInvite(savedMatch.getId(), InviteType.TEAM);
        String teamInviteUrl = inviteService.buildInviteUrl(teamInvite.getInviteToken());

        // Generate EMERGENCY invite (conditional)
        String emergencyInviteUrl = null;
        if (dto.getEmergencyEnabled()) {
            MatchInvite emergencyInvite = inviteService.createInvite(savedMatch.getId(), InviteType.EMERGENCY);
            emergencyInviteUrl = inviteService.buildInviteUrl(emergencyInvite.getInviteToken());
        }

        return MatchCreatedResponseDto.builder()
            .matchId(savedMatch.getId())
            .teamInviteUrl(teamInviteUrl)
            .emergencyInviteUrl(emergencyInviteUrl)
            .build();
    }

    @Override
    public MatchResponseDto getMatch(UUID matchId, Long userId) {
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new MvpException(MvpError.MATCH_NOT_FOUND));

        boolean isCaptain = userId != null && match.isCaptain(userId);

        // Count participants
        long teamCount = participantRepository.countByMatchIdAndStatusConfirmedAndRole(matchId, ParticipantRole.TEAM);
        long backupCount = participantRepository.countByMatchIdAndStatusConfirmedAndRole(matchId, ParticipantRole.BACKUP);
        long emergencyCount = participantRepository.countByMatchIdAndStatusConfirmedAndRole(matchId, ParticipantRole.EMERGENCY);

        MatchResponseDto.MatchResponseDtoBuilder responseBuilder = MatchResponseDto.builder()
            .matchId(match.getId())
            .teamName(match.getTeamName())
            .eventType(match.getEventType())
            .ballCategory(match.getBallCategory())
            .ballVariant(match.getBallVariant())
            .groundMapsUrl(match.getGroundMapsUrl())
            .groundLat(match.getGroundLat())
            .groundLng(match.getGroundLng())
            .overs(match.getOvers())
            .feePerPerson(match.getFeePerPerson())
            .emergencyFee(match.getEmergencyFee())
            .requiredPlayers(match.getRequiredPlayers())
            .backupSlots(match.getBackupSlots())
            .emergencyEnabled(match.getEmergencyEnabled())
            .status(match.getStatus())
            .startTime(match.getStartTime())
            .createdAt(match.getCreatedAt())
            .teamCount((int) teamCount)
            .backupCount((int) backupCount)
            .emergencyCount((int) emergencyCount);

        // Captain-only fields
        if (isCaptain) {
            MvpUser captain = mvpUserRepository.findById(match.getCreatedBy())
                .orElse(null);

            if (captain != null) {
                responseBuilder
                    .captainId(captain.getId())
                    .captainName(captain.getName())
                    .captainPhone(captain.getPhoneNumber());
            }

            // Full participant list
            List<MatchParticipant> participants = participantRepository.findByMatchId(matchId);
            List<MatchResponseDto.ParticipantDto> participantDtos = participants.stream()
                .map(this::mapParticipantToDto)
                .collect(Collectors.toList());

            responseBuilder.participants(participantDtos);
        }

        return responseBuilder.build();
    }

    @Override
    @Transactional
    public void respondYes(UUID matchId, Long userId) {
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new MvpException(MvpError.MATCH_NOT_FOUND));

        // Check match status
        if (match.getStatus() != MatchStatus.CREATED && match.getStatus() != MatchStatus.ACTIVE) {
            throw new MvpException(MvpError.INVALID_MATCH_STATUS);
        }

        // Check if user already responded
        Optional<MatchParticipant> existingParticipant =
            participantRepository.findByMatchIdAndUserId(matchId, userId);

        if (existingParticipant.isPresent()) {
            // Idempotent: update existing participant
            MatchParticipant participant = existingParticipant.get();
            if (participant.getStatus() == ParticipantStatus.BACKED_OUT) {
                participant.setStatus(ParticipantStatus.CONFIRMED);
                participantRepository.save(participant);
                log.info("User {} rejoined match {}", userId, matchId);
            } else {
                log.debug("User {} already confirmed for match {}", userId, matchId);
            }
            return;
        }

        // Determine role based on current participant count
        long confirmedCount = participantRepository.countConfirmedByMatchId(matchId);
        ParticipantRole role;

        if (confirmedCount < match.getRequiredPlayers()) {
            role = ParticipantRole.TEAM;
        } else if (confirmedCount < match.getRequiredPlayers() + match.getBackupSlots()) {
            role = ParticipantRole.BACKUP;
        } else {
            throw new MvpException(MvpError.MATCH_FULL);
        }

        // Create participant
        MatchParticipant participant = MatchParticipant.builder()
            .matchId(matchId)
            .userId(userId)
            .role(role)
            .status(ParticipantStatus.CONFIRMED)
            .feeAmount(match.getFeePerPerson())
            .paymentStatus(PaymentStatus.UNPAID)
            .build();

        participantRepository.save(participant);
        log.info("User {} confirmed for match {} with role {}", userId, matchId, role);

        // Remove from unavailability if previously declined
        unavailabilityRepository.findByMatchIdAndUserId(matchId, userId)
            .ifPresent(unavailabilityRepository::delete);
    }

    @Override
    @Transactional
    public void respondNo(UUID matchId, Long userId) {
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new MvpException(MvpError.MATCH_NOT_FOUND));

        // Remove from participants if exists
        participantRepository.findByMatchIdAndUserId(matchId, userId)
            .ifPresent(participant -> {
                participant.setStatus(ParticipantStatus.BACKED_OUT);
                participantRepository.save(participant);
            });

        // Mark as unavailable
        if (!unavailabilityRepository.existsByMatchIdAndUserId(matchId, userId)) {
            MatchUnavailability unavailability = MatchUnavailability.builder()
                .matchId(matchId)
                .userId(userId)
                .build();

            unavailabilityRepository.save(unavailability);
            log.info("User {} marked unavailable for match {}", userId, matchId);
        }
    }

    @Override
    @Transactional
    public void completeMatch(UUID matchId, Long captainId) {
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new MvpException(MvpError.MATCH_NOT_FOUND));

        // Verify captain
        if (!match.isCaptain(captainId)) {
            throw new MvpException(MvpError.NOT_CAPTAIN);
        }

        // Check status
        if (match.getStatus() == MatchStatus.COMPLETED) {
            throw new MvpException(MvpError.MATCH_ALREADY_COMPLETED);
        }

        // Update status
        match.setStatus(MatchStatus.COMPLETED);
        matchRepository.save(match);

        // Record platform fee
        if (!platformFeeLogRepository.existsByMatchId(matchId)) {
            PlatformFeeLog feeLog = PlatformFeeLog.builder()
                .matchId(matchId)
                .amount(platformFee)
                .status("RECORDED")
                .build();

            platformFeeLogRepository.save(feeLog);
            log.info("Platform fee recorded for match {}: ₹{}", matchId, platformFee);
        }

        log.info("Match {} marked as COMPLETED by captain {}", matchId, captainId);
    }

    @Override
    @Transactional
    public void cancelMatch(UUID matchId, Long captainId) {
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new MvpException(MvpError.MATCH_NOT_FOUND));

        // Verify captain
        if (!match.isCaptain(captainId)) {
            throw new MvpException(MvpError.NOT_CAPTAIN);
        }

        // Update status
        match.setStatus(MatchStatus.CANCELLED);
        matchRepository.save(match);

        log.info("Match {} cancelled by captain {}", matchId, captainId);
    }

    @Override
    @Transactional(readOnly = true)
    public MyGamesResponseDto getMyGames(Long userId) {
        // Fetch all matches where user is captain or participant
        List<Match> matches = matchRepository.findAllUserMatches(userId);

        log.info("Found {} matches for user {}", matches.size(), userId);

        // Convert to DTOs
        List<MyGamesResponseDto.GameSummaryDto> gameSummaries = matches.stream()
            .map(match -> buildGameSummary(match, userId))
            .collect(Collectors.toList());

        // Calculate stats
        int totalCount = gameSummaries.size();
        int upcomingCount = (int) gameSummaries.stream()
            .filter(g -> g.getStatus() == MatchStatus.CREATED || g.getStatus() == MatchStatus.ACTIVE)
            .count();
        int completedCount = (int) gameSummaries.stream()
            .filter(g -> g.getStatus() == MatchStatus.COMPLETED)
            .count();
        int cancelledCount = (int) gameSummaries.stream()
            .filter(g -> g.getStatus() == MatchStatus.CANCELLED)
            .count();

        return MyGamesResponseDto.builder()
            .games(gameSummaries)
            .totalCount(totalCount)
            .upcomingCount(upcomingCount)
            .completedCount(completedCount)
            .cancelledCount(cancelledCount)
            .build();
    }

    private MyGamesResponseDto.GameSummaryDto buildGameSummary(Match match, Long userId) {
        // Determine user's role
        boolean isCaptain = match.isCaptain(userId);
        Optional<MatchParticipant> userParticipant = participantRepository.findByMatchIdAndUserId(match.getId(), userId);

        String userRole;
        String paymentStatus = null;
        String paymentMode = null;
        Integer feeAmount = null;

        if (isCaptain) {
            userRole = "CAPTAIN";
        } else if (userParticipant.isPresent()) {
            MatchParticipant participant = userParticipant.get();
            userRole = participant.getRole().name();
            paymentStatus = participant.getPaymentStatus().name();
            paymentMode = participant.getPaymentMode() != null ? participant.getPaymentMode().name() : null;
            feeAmount = participant.getFeeAmount();
        } else {
            // Shouldn't happen, but handle gracefully
            userRole = "UNKNOWN";
        }

        // Get participant counts
        long teamCount = participantRepository.countByMatchIdAndRoleAndStatus(
            match.getId(), ParticipantRole.TEAM, ParticipantStatus.CONFIRMED);
        long backupCount = participantRepository.countByMatchIdAndRoleAndStatus(
            match.getId(), ParticipantRole.BACKUP, ParticipantStatus.CONFIRMED);
        long emergencyCount = participantRepository.countByMatchIdAndRoleAndStatus(
            match.getId(), ParticipantRole.EMERGENCY, ParticipantStatus.CONFIRMED);

        return MyGamesResponseDto.GameSummaryDto.builder()
            .matchId(match.getId())
            .teamName(match.getTeamName())
            .eventType(match.getEventType())
            .ballCategory(match.getBallCategory())
            .ballVariant(match.getBallVariant())
            .overs(match.getOvers())
            .status(match.getStatus())
            .startTime(match.getStartTime())
            .groundMapsUrl(match.getGroundMapsUrl())
            .groundLat(match.getGroundLat())
            .groundLng(match.getGroundLng())
            .feePerPerson(match.getFeePerPerson())
            .emergencyFee(match.getEmergencyFee())
            .userRole(userRole)
            .isCaptain(isCaptain)
            .teamCount((int) teamCount)
            .backupCount((int) backupCount)
            .emergencyCount((int) emergencyCount)
            .requiredPlayers(match.getRequiredPlayers())
            .backupSlots(match.getBackupSlots())
            .paymentStatus(paymentStatus)
            .paymentMode(paymentMode)
            .feeAmount(feeAmount)
            .build();
    }

    private MatchResponseDto.ParticipantDto mapParticipantToDto(MatchParticipant participant) {
        MvpUser user = mvpUserRepository.findById(participant.getUserId())
            .orElse(null);

        return MatchResponseDto.ParticipantDto.builder()
            .userId(participant.getUserId())
            .name(user != null ? user.getName() : "Unknown")
            .phoneNumber(user != null ? user.getPhoneNumber() : null)
            .role(participant.getRole())
            .status(participant.getStatus())
            .feeAmount(participant.getFeeAmount())
            .paymentStatus(participant.getPaymentStatus())
            .paymentMode(participant.getPaymentMode())
            .build();
    }
}
