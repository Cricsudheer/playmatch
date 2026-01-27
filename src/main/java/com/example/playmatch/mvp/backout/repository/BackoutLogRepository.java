package com.example.playmatch.mvp.backout.repository;

import com.example.playmatch.mvp.backout.model.BackoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BackoutLogRepository extends JpaRepository<BackoutLog, Long> {
    List<BackoutLog> findByMatchId(UUID matchId);
    List<BackoutLog> findByUserId(Long userId);
}
