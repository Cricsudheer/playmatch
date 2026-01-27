package com.example.playmatch.mvp.backout.service.impl;

import com.example.playmatch.mvp.backout.model.BackoutLog;
import com.example.playmatch.mvp.backout.model.BackoutReason;
import com.example.playmatch.mvp.backout.repository.BackoutLogRepository;
import com.example.playmatch.mvp.backout.service.BackoutService;
import com.example.playmatch.mvp.common.error.MvpError;
import com.example.playmatch.mvp.common.exception.MvpException;
import com.example.playmatch.mvp.matches.model.Match;
import com.example.playmatch.mvp.matches.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackoutServiceImpl implements BackoutService {

    private final BackoutLogRepository backoutLogRepository;
    private final MatchRepository matchRepository;

    @Override
    @Transactional
    public void logBackout(UUID matchId, Long userId, BackoutReason reason, String notes, Long captainId) {
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new MvpException(MvpError.MATCH_NOT_FOUND));

        // Verify captain
        if (!match.isCaptain(captainId)) {
            throw new MvpException(MvpError.NOT_CAPTAIN);
        }

        BackoutLog backoutLog = BackoutLog.builder()
            .matchId(matchId)
            .userId(userId)
            .reason(reason)
            .notes(notes)
            .loggedBy(captainId)
            .build();

        backoutLogRepository.save(backoutLog);

        log.info("Backout logged: matchId={}, userId={}, reason={}, loggedBy={}",
            matchId, userId, reason, captainId);
    }
}
