package com.example.playmatch.team.repository;

import com.example.playmatch.team.model.TeamMember;
import com.example.playmatch.team.model.enums.TeamRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);

    @Query("SELECT tm FROM TeamMember tm " +
           "JOIN FETCH tm.user " +
           "WHERE tm.team.id = :teamId " +
           "AND (:role IS NULL OR tm.role = :role)")
    Page<TeamMember> findByTeamIdAndRole(@Param("teamId") Long teamId,
                                         @Param("role") TeamRole role,
                                         Pageable pageable);

    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

    /**
     * Check if a user is an active member of a team.
     */
    @Query("SELECT CASE WHEN COUNT(tm) > 0 THEN true ELSE false END FROM TeamMember tm " +
           "WHERE tm.team.id = :teamId AND tm.userId = :userId AND tm.team.isActive = true")
    boolean existsByTeamIdAndUserIdAndIsActiveTrue(@Param("teamId") Long teamId, @Param("userId") Long userId);

    /**
     * Find all active members of a team.
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.team.isActive = true")
    List<TeamMember> findByTeamIdAndIsActiveTrue(@Param("teamId") Long teamId);

    /**
     * Find all members of a team (regardless of team active status).
     */
    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.user WHERE tm.team.id = :teamId")
    List<TeamMember> findByTeamId(@Param("teamId") Long teamId);

    /**
     * Delete all members of a team.
     */
    void deleteByTeamId(Long teamId);

}
