package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.TeamInvite;
import com.fiap.projects.apipassabola.entity.TeamInvite.InviteStatus;
import com.fiap.projects.apipassabola.entity.Team;
import com.fiap.projects.apipassabola.entity.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamInviteRepository extends JpaRepository<TeamInvite, Long> {
    
    // Find invites by team
    List<TeamInvite> findByTeam(Team team);
    
    // Find invites by team and status
    List<TeamInvite> findByTeamAndStatus(Team team, InviteStatus status);
    
    // Find invites sent to a specific player
    List<TeamInvite> findByInvitedPlayer(Player invitedPlayer);
    
    // Find pending invites for a specific player
    List<TeamInvite> findByInvitedPlayerAndStatus(Player invitedPlayer, InviteStatus status);
    
    // Find invites sent by a specific player (inviter)
    List<TeamInvite> findByInviter(Player inviter);
    
    // Check if there's already a pending invite for a player to a team
    @Query("SELECT ti FROM TeamInvite ti WHERE ti.team.id = :teamId AND ti.invitedPlayer.id = :playerId AND ti.status = :status")
    Optional<TeamInvite> findByTeamIdAndInvitedPlayerIdAndStatus(
        @Param("teamId") Long teamId, 
        @Param("playerId") Long playerId, 
        @Param("status") InviteStatus status
    );
    
    // Find all pending invites for a team
    @Query("SELECT ti FROM TeamInvite ti WHERE ti.team.id = :teamId AND ti.status = 'PENDING'")
    List<TeamInvite> findPendingInvitesByTeamId(@Param("teamId") Long teamId);
    
    // Find all pending invites for a player
    @Query("SELECT ti FROM TeamInvite ti WHERE ti.invitedPlayer.id = :playerId AND ti.status = 'PENDING'")
    List<TeamInvite> findPendingInvitesByPlayerId(@Param("playerId") Long playerId);
    
    // Count pending invites for a team
    @Query("SELECT COUNT(ti) FROM TeamInvite ti WHERE ti.team.id = :teamId AND ti.status = 'PENDING'")
    long countPendingInvitesByTeamId(@Param("teamId") Long teamId);
    
    // Count pending invites for a player
    @Query("SELECT COUNT(ti) FROM TeamInvite ti WHERE ti.invitedPlayer.id = :playerId AND ti.status = 'PENDING'")
    long countPendingInvitesByPlayerId(@Param("playerId") Long playerId);
    
    // Find invites with pagination
    Page<TeamInvite> findByInvitedPlayerAndStatus(Player invitedPlayer, InviteStatus status, Pageable pageable);
}
