package com.example.playmatch.sigma.repository;

import com.example.playmatch.sigma.model.BowlingStats;
import com.example.playmatch.sigma.model.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BowlingStatsRepository extends JpaRepository<BowlingStats, Long> {

    List<BowlingStats> findByPlayer(Player player);

    List<BowlingStats> findByPlayer_Id(Long playerId);

    Page<BowlingStats> findByPlayer_Id(Long playerId, Pageable pageable);

    @Query("SELECT b FROM BowlingStats b WHERE b.player.id = :playerId ORDER BY b.innings DESC")
    List<BowlingStats> findByPlayerIdOrderByInningsDesc(@Param("playerId") Long playerId);
}

