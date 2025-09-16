package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.GameInvite;
import com.fiap.projects.apipassabola.entity.Game;
import com.fiap.projects.apipassabola.entity.Organization;
import com.fiap.projects.apipassabola.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameInviteRepository extends JpaRepository<GameInvite, Long> {
    
    // Find invites by game
    List<GameInvite> findByGame(Game game);
    Page<GameInvite> findByGame(Game game, Pageable pageable);
    
    // Find invites by game ID
    List<GameInvite> findByGameId(Long gameId);
    Page<GameInvite> findByGameId(Long gameId, Pageable pageable);
    
    // Find invites by inviting organization
    List<GameInvite> findByInvitingOrganization(Organization organization);
    Page<GameInvite> findByInvitingOrganization(Organization organization, Pageable pageable);
    
    // Find invites by inviting organization ID
    List<GameInvite> findByInvitingOrganizationId(Long organizationId);
    Page<GameInvite> findByInvitingOrganizationId(Long organizationId, Pageable pageable);
    
    // Find invites by invited team
    List<GameInvite> findByInvitedTeam(Team team);
    Page<GameInvite> findByInvitedTeam(Team team, Pageable pageable);
    
    // Find invites by invited team ID
    List<GameInvite> findByInvitedTeamId(Long teamId);
    Page<GameInvite> findByInvitedTeamId(Long teamId, Pageable pageable);
    
    // Find invites by status
    List<GameInvite> findByStatus(GameInvite.InviteStatus status);
    Page<GameInvite> findByStatus(GameInvite.InviteStatus status, Pageable pageable);
    
    // Find invites by game and status
    List<GameInvite> findByGameIdAndStatus(Long gameId, GameInvite.InviteStatus status);
    
    // Find invites by team and status
    List<GameInvite> findByInvitedTeamIdAndStatus(Long teamId, GameInvite.InviteStatus status);
    Page<GameInvite> findByInvitedTeamIdAndStatus(Long teamId, GameInvite.InviteStatus status, Pageable pageable);
    
    // Find invites by organization and status
    List<GameInvite> findByInvitingOrganizationIdAndStatus(Long organizationId, GameInvite.InviteStatus status);
    
    // Check if invite already exists
    boolean existsByGameIdAndInvitedTeamIdAndStatus(Long gameId, Long teamId, GameInvite.InviteStatus status);
    
    // Find specific invite
    Optional<GameInvite> findByGameIdAndInvitedTeamId(Long gameId, Long teamId);
    
    // Find pending invites for a team
    @Query("SELECT gi FROM GameInvite gi WHERE gi.invitedTeam.id = :teamId AND gi.status = 'PENDING' AND gi.game.gameDate > :currentTime")
    List<GameInvite> findPendingInvitesForTeam(@Param("teamId") Long teamId, @Param("currentTime") LocalDateTime currentTime);
    
    // Find pending invites for a game
    @Query("SELECT gi FROM GameInvite gi WHERE gi.game.id = :gameId AND gi.status = 'PENDING'")
    List<GameInvite> findPendingInvitesForGame(@Param("gameId") Long gameId);
    
    // Find accepted invites for a game
    @Query("SELECT gi FROM GameInvite gi WHERE gi.game.id = :gameId AND gi.status = 'ACCEPTED'")
    List<GameInvite> findAcceptedInvitesForGame(@Param("gameId") Long gameId);
    
    // Count pending invites by game
    long countByGameIdAndStatus(Long gameId, GameInvite.InviteStatus status);
    
    // Count pending invites by team
    long countByInvitedTeamIdAndStatus(Long teamId, GameInvite.InviteStatus status);
    
    // Find invites by team position
    List<GameInvite> findByGameIdAndTeamPosition(Long gameId, String teamPosition);
    
    // Find expired invites
    @Query("SELECT gi FROM GameInvite gi WHERE gi.status = 'PENDING' AND gi.game.gameDate < :currentTime")
    List<GameInvite> findExpiredInvites(@Param("currentTime") LocalDateTime currentTime);
    
    // Find invites for games created by organization
    @Query("SELECT gi FROM GameInvite gi WHERE gi.game.hostId = :organizationId")
    List<GameInvite> findInvitesForGamesCreatedByOrganization(@Param("organizationId") Long organizationId);
}
