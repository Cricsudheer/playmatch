package com.example.playmatch.team.repository;

import com.example.playmatch.team.model.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByIdAndIsActiveTrue(Long id);

    @Query("SELECT t FROM Team t WHERE " +
           "(:city IS NULL OR LOWER(t.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "t.isActive = true")
    Page<Team> findTeamsByCriteria(@Param("city") String city,
                                   @Param("name") String name,
                                   Pageable pageable);

    boolean existsByNameIgnoreCase(String name);
}
