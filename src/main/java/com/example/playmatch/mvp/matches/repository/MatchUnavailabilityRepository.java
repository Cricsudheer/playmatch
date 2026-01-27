package com.example.playmatch.mvp.matches.repository;

import com.example.playmatch.mvp.matches.model.MatchUnavailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchUnavailabilityRepository extends JpaRepository<MatchUnavailability, Long> {
    Optional<MatchUnavailability> findByMatchIdAndUserId(UUID matchId, Long userId);
    boolean existsByMatchIdAndUserId(UUID matchId, Long userId);
}
