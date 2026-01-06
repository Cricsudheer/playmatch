package com.example.playmatch.sigma.repository;

import com.example.playmatch.sigma.model.DismissalStats;
import com.example.playmatch.sigma.model.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DismissalStatsRepository extends JpaRepository<DismissalStats, Long> {

    List<DismissalStats> findByPlayer(Player player);

    List<DismissalStats> findByPlayer_Id(Long playerId);

    Page<DismissalStats> findByPlayer_Id(Long playerId, Pageable pageable);

    @Query("SELECT d FROM DismissalStats d WHERE d.player.id = :playerId ORDER BY d.innings DESC")
    List<DismissalStats> findByPlayerIdOrderByInningsDesc(@Param("playerId") Long playerId);

    @Query("SELECT d FROM DismissalStats d INNER JOIN FETCH d.player p ORDER BY p.id, d.innings DESC")
    List<DismissalStats> findAllWithPlayerOptimized();
}
