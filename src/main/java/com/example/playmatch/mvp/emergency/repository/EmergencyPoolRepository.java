package com.example.playmatch.mvp.emergency.repository;

import com.example.playmatch.mvp.emergency.model.EmergencyPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyPoolRepository extends JpaRepository<EmergencyPool, Long> {
    Optional<EmergencyPool> findByUserId(Long userId);
    List<EmergencyPool> findByAreaAndActiveTrue(String area);
}
