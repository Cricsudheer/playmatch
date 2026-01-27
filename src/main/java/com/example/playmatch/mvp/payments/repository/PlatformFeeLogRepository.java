package com.example.playmatch.mvp.payments.repository;

import com.example.playmatch.mvp.payments.model.PlatformFeeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformFeeLogRepository extends JpaRepository<PlatformFeeLog, Long> {
    Optional<PlatformFeeLog> findByMatchId(UUID matchId);
    boolean existsByMatchId(UUID matchId);
}
