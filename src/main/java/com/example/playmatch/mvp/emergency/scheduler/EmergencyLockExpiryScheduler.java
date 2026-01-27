package com.example.playmatch.mvp.emergency.scheduler;

import com.example.playmatch.mvp.emergency.model.EmergencyRequest;
import com.example.playmatch.mvp.emergency.model.EmergencyRequestStatus;
import com.example.playmatch.mvp.emergency.repository.EmergencyRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmergencyLockExpiryScheduler {

    private final EmergencyRequestRepository emergencyRequestRepository;

    /**
     * Expire emergency requests with locks that have passed their expiry time
     * Runs every 5 minutes (300,000 milliseconds)
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void expireLockedRequests() {
        log.debug("Running emergency lock expiry scheduler");

        List<EmergencyRequest> expired = emergencyRequestRepository
            .findByStatusAndLockExpiresAtBefore(
                EmergencyRequestStatus.REQUESTED,
                OffsetDateTime.now()
            );

        if (expired.isEmpty()) {
            log.debug("No expired emergency requests found");
            return;
        }

        // Update status to EXPIRED
        expired.forEach(request -> {
            request.setStatus(EmergencyRequestStatus.EXPIRED);
            log.info("Expired emergency request: matchId={}, userId={}, requestId={}",
                request.getMatchId(), request.getUserId(), request.getId());
        });

        emergencyRequestRepository.saveAll(expired);

        log.info("Expired {} emergency request(s)", expired.size());
    }
}
