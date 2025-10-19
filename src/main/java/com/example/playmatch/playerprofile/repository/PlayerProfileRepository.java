package com.example.playmatch.playerprofile.repository;

import com.example.playmatch.playerprofile.model.PlayerProfile;
import com.example.playmatch.api.model.PrimaryRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, Long> {

    Optional<PlayerProfile> findByUser_Id(UUID userId);

    boolean existsByUser_Id(UUID userId);

    boolean existsByMobile(String mobile);

    @Query("SELECT p FROM PlayerProfile p WHERE " +
           "(:city IS NULL OR LOWER(p.city) = LOWER(:city)) AND " +
           "(:primaryRole IS NULL OR p.primaryRole = :primaryRole)")
    Page<PlayerProfile> findByFilters(@Param("city") String city,
                                    @Param("primaryRole") PrimaryRole primaryRole,
                                    Pageable pageable);
}
