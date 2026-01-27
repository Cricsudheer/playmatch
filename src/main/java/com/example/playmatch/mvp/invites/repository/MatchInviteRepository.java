package com.example.playmatch.mvp.invites.repository;

import com.example.playmatch.mvp.invites.model.InviteType;
import com.example.playmatch.mvp.invites.model.MatchInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchInviteRepository extends JpaRepository<MatchInvite, Long> {
    Optional<MatchInvite> findByInviteToken(String inviteToken);
    boolean existsByInviteToken(String inviteToken);
    List<MatchInvite> findByMatchId(UUID matchId);
    Optional<MatchInvite> findByMatchIdAndType(UUID matchId, InviteType type);
}
