package com.example.playmatch.mvp.emergency.repository;

import com.example.playmatch.mvp.emergency.model.EmergencyRequest;
import com.example.playmatch.mvp.emergency.model.EmergencyRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmergencyRequestRepository extends JpaRepository<EmergencyRequest, Long> {
    List<EmergencyRequest> findByMatchIdAndStatus(UUID matchId, EmergencyRequestStatus status);

    Optional<EmergencyRequest> findByUserIdAndStatus(Long userId, EmergencyRequestStatus status);

    List<EmergencyRequest> findByStatusAndLockExpiresAtBefore(EmergencyRequestStatus status, OffsetDateTime now);

    boolean existsByUserIdAndStatus(Long userId, EmergencyRequestStatus status);
}
