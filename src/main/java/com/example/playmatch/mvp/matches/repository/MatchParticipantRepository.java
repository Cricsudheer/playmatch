package com.example.playmatch.mvp.matches.repository;

import com.example.playmatch.mvp.matches.model.MatchParticipant;
import com.example.playmatch.mvp.matches.model.ParticipantRole;
import com.example.playmatch.mvp.matches.model.ParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchParticipantRepository extends JpaRepository<MatchParticipant, Long> {
    Optional<MatchParticipant> findByMatchIdAndUserId(UUID matchId, Long userId);

    List<MatchParticipant> findByMatchId(UUID matchId);

    @Query("SELECT p FROM MatchParticipant p JOIN FETCH p.user WHERE p.matchId = :matchId")
    List<MatchParticipant> findByMatchIdWithUser(@Param("matchId") UUID matchId);

    List<MatchParticipant> findByMatchIdAndStatus(UUID matchId, ParticipantStatus status);

    @Query("SELECT COUNT(p) FROM MatchParticipant p WHERE p.matchId = :matchId AND p.status = 'CONFIRMED' AND p.role = :role")
    long countByMatchIdAndStatusConfirmedAndRole(@Param("matchId") UUID matchId, @Param("role") ParticipantRole role);

    @Query("SELECT COUNT(p) FROM MatchParticipant p WHERE p.matchId = :matchId AND p.status = 'CONFIRMED'")
    long countConfirmedByMatchId(@Param("matchId") UUID matchId);

    /**
     * Count participants by match, role, and status
     */
    long countByMatchIdAndRoleAndStatus(UUID matchId, ParticipantRole role, ParticipantStatus status);
}
