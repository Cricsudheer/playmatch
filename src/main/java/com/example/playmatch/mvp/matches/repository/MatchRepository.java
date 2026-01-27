package com.example.playmatch.mvp.matches.repository;

import com.example.playmatch.mvp.matches.model.Match;
import com.example.playmatch.mvp.matches.model.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, UUID> {
    List<Match> findByCreatedBy(Long userId);
    List<Match> findByCreatedByAndStatus(Long userId, MatchStatus status);

    /**
     * Find all matches where user is either captain or a confirmed participant
     */
    @Query("""
        SELECT DISTINCT m FROM Match m
        LEFT JOIN MatchParticipant mp ON m.id = mp.matchId AND mp.userId = :userId
        WHERE m.createdBy = :userId OR mp.id IS NOT NULL
        ORDER BY m.startTime DESC
        """)
    List<Match> findAllUserMatches(@Param("userId") Long userId);
}
