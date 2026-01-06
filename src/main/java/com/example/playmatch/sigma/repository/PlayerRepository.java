package com.example.playmatch.sigma.repository;

import com.example.playmatch.sigma.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByName(String name);

    boolean existsByName(String name);
}

