package com.example.playmatch.sigma.repository;

import com.example.playmatch.sigma.model.BattingStats;
import com.example.playmatch.sigma.model.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BattingStatsRepository extends JpaRepository<BattingStats, Long> {

    List<BattingStats> findByPlayer(Player player);

    List<BattingStats> findByPlayer_Id(Long playerId);

    Page<BattingStats> findByPlayer_Id(Long playerId, Pageable pageable);

    @Query("SELECT b FROM BattingStats b WHERE b.player.id = :playerId ORDER BY b.innings DESC")
    List<BattingStats> findByPlayerIdOrderByInningsDesc(@Param("playerId") Long playerId);
}

