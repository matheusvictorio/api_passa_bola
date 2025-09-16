package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.entity.Team;
import com.fiap.projects.apipassabola.entity.TeamInvite;
import com.fiap.projects.apipassabola.entity.TeamInvite.InviteStatus;
import com.fiap.projects.apipassabola.entity.Player;
import com.fiap.projects.apipassabola.repository.TeamRepository;
import com.fiap.projects.apipassabola.repository.TeamInviteRepository;
import com.fiap.projects.apipassabola.repository.PlayerRepository;
import com.fiap.projects.apipassabola.dto.response.TeamResponse;
import com.fiap.projects.apipassabola.dto.response.TeamSummaryResponse;
import com.fiap.projects.apipassabola.dto.response.TeamInviteResponse;
import com.fiap.projects.apipassabola.dto.response.PlayerSummaryResponse;
import com.fiap.projects.apipassabola.exception.BusinessException;
import com.fiap.projects.apipassabola.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeamService {
    
    private final TeamRepository teamRepository;
    private final TeamInviteRepository teamInviteRepository;
    private final PlayerRepository playerRepository;
    private final UserContextService userContextService;
    
    // Create a new team (only players can create teams)
    public Team createTeam(String teamName) {
        // Get current authenticated player
        Player currentPlayer = userContextService.getCurrentPlayer();
        
        // Validate team name uniqueness
        if (teamRepository.existsByNameTeam(teamName)) {
            throw new BusinessException("Team name already exists: " + teamName);
        }
        
        // Create team with current player as leader
        Team team = new Team();
        team.setNameTeam(teamName);
        team.setLeader(currentPlayer);
        
        Team savedTeam = teamRepository.save(team);
        
        // Add leader to team players list
        savedTeam.addPlayer(currentPlayer);
        teamRepository.save(savedTeam);
        
        log.info("Team created: {} by player: {}", teamName, currentPlayer.getRealUsername());
        return savedTeam;
    }
    
    // Send invite to a player (only team leader can send invites)
    public TeamInvite sendInvite(Long teamId, Long invitedPlayerId) {
        // Get current authenticated player
        Player currentPlayer = userContextService.getCurrentPlayer();
        
        // Get team
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));
        
        // Validate that current player is the team leader
        if (!team.isLeader(currentPlayer)) {
            throw new BusinessException("Only team leader can send invites");
        }
        
        // Get invited player
        Player invitedPlayer = playerRepository.findById(invitedPlayerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", invitedPlayerId));
        
        // Validate mutual following (both players must follow each other)
        if (!areMutuallyFollowing(currentPlayer, invitedPlayer)) {
            throw new BusinessException("You can only invite players that you follow mutually");
        }
        
        // Check if player is already in this specific team
        if (team.hasPlayer(invitedPlayer)) {
            throw new BusinessException("Player is already in this team");
        }
        
        // Check if there's already a pending invite for this player to this team
        Optional<TeamInvite> existingInvite = teamInviteRepository
                .findByTeamIdAndInvitedPlayerIdAndStatus(teamId, invitedPlayerId, InviteStatus.PENDING);
        
        if (existingInvite.isPresent()) {
            throw new BusinessException("There's already a pending invite for this player");
        }
        
        // Create invite
        TeamInvite invite = new TeamInvite();
        invite.setTeam(team);
        invite.setInviter(currentPlayer);
        invite.setInvitedPlayer(invitedPlayer);
        invite.setStatus(InviteStatus.PENDING);
        
        TeamInvite savedInvite = teamInviteRepository.save(invite);
        
        log.info("Team invite sent from {} to {} for team: {}", 
                currentPlayer.getRealUsername(), invitedPlayer.getRealUsername(), team.getNameTeam());
        
        return savedInvite;
    }
    
    // Accept team invite
    public Team acceptInvite(Long inviteId) {
        // Get current authenticated player
        Player currentPlayer = userContextService.getCurrentPlayer();
        
        // Get invite
        TeamInvite invite = teamInviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("TeamInvite", "id", inviteId));
        
        // Validate that current player is the invited player
        if (!invite.getInvitedPlayer().getId().equals(currentPlayer.getId())) {
            throw new BusinessException("You can only accept invites sent to you");
        }
        
        // Validate invite is still pending
        if (!invite.isPending()) {
            throw new BusinessException("Invite is no longer pending");
        }
        
        // Check if player is already in this specific team
        if (invite.getTeam().hasPlayer(currentPlayer)) {
            throw new BusinessException("You are already in this team");
        }
        
        // Accept invite
        invite.setStatus(InviteStatus.ACCEPTED);
        teamInviteRepository.save(invite);
        
        // Add player to team
        Team team = invite.getTeam();
        team.addPlayer(currentPlayer);
        teamRepository.save(team);
        
        log.info("Player {} accepted invite to team: {}", 
                currentPlayer.getRealUsername(), team.getNameTeam());
        
        return team;
    }
    
    // Reject team invite
    public void rejectInvite(Long inviteId) {
        // Get current authenticated player
        Player currentPlayer = userContextService.getCurrentPlayer();
        
        // Get invite
        TeamInvite invite = teamInviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("TeamInvite", "id", inviteId));
        
        // Validate that current player is the invited player
        if (!invite.getInvitedPlayer().getId().equals(currentPlayer.getId())) {
            throw new BusinessException("You can only reject invites sent to you");
        }
        
        // Validate invite is still pending
        if (!invite.isPending()) {
            throw new BusinessException("Invite is no longer pending");
        }
        
        // Reject invite
        invite.setStatus(InviteStatus.REJECTED);
        teamInviteRepository.save(invite);
        
        log.info("Player {} rejected invite to team: {}", 
                currentPlayer.getRealUsername(), invite.getTeam().getNameTeam());
    }
    
    // Cancel invite (only team leader can cancel)
    public void cancelInvite(Long inviteId) {
        // Get current authenticated player
        Player currentPlayer = userContextService.getCurrentPlayer();
        
        // Get invite
        TeamInvite invite = teamInviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("TeamInvite", "id", inviteId));
        
        // Validate that current player is the inviter (team leader)
        if (!invite.getInviter().getId().equals(currentPlayer.getId())) {
            throw new BusinessException("Only the inviter can cancel the invite");
        }
        
        // Validate invite is still pending
        if (!invite.isPending()) {
            throw new BusinessException("Invite is no longer pending");
        }
        
        // Cancel invite
        invite.setStatus(InviteStatus.CANCELLED);
        teamInviteRepository.save(invite);
        
        log.info("Player {} cancelled invite to {} for team: {}", 
                currentPlayer.getRealUsername(), 
                invite.getInvitedPlayer().getRealUsername(), 
                invite.getTeam().getNameTeam());
    }
    
    // Leave team
    public void leaveTeam(Long teamId) {
        // Get current authenticated player
        Player currentPlayer = userContextService.getCurrentPlayer();
        
        // Get team
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));
        
        if (!team.hasPlayer(currentPlayer)) {
            throw new BusinessException("You are not in this team");
        }
        
        // If player is leader, they cannot leave (must transfer leadership or disband team)
        if (team.isLeader(currentPlayer)) {
            throw new BusinessException("Team leader cannot leave. Transfer leadership or disband team first");
        }
        
        // Remove player from team
        team.removePlayer(currentPlayer);
        teamRepository.save(team);
        
        log.info("Player {} left team: {}", currentPlayer.getRealUsername(), team.getNameTeam());
    }
    
    // Remove player from team (only team leader can remove players)
    public void removePlayerFromTeam(Long teamId, Long playerId) {
        // Get current authenticated player
        Player currentPlayer = userContextService.getCurrentPlayer();
        
        // Get team
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));
        
        // Validate that current player is the team leader
        if (!team.isLeader(currentPlayer)) {
            throw new BusinessException("Only team leader can remove players");
        }
        
        // Get player to remove
        Player playerToRemove = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", playerId));
        
        // Validate player is in the team
        if (!team.hasPlayer(playerToRemove)) {
            throw new BusinessException("Player is not in this team");
        }
        
        // Leader cannot remove themselves
        if (playerToRemove.getId().equals(currentPlayer.getId())) {
            throw new BusinessException("Leader cannot remove themselves. Transfer leadership or disband team first");
        }
        
        // Remove player from team
        team.removePlayer(playerToRemove);
        teamRepository.save(team);
        
        log.info("Player {} removed {} from team: {}", 
                currentPlayer.getRealUsername(), 
                playerToRemove.getRealUsername(), 
                team.getNameTeam());
    }
    
    // Get team by id
    public Team findById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));
    }
    
    // Get all teams with pagination
    public Page<Team> findAll(Pageable pageable) {
        return teamRepository.findAll(pageable);
    }
    
    // Search teams by name
    public Page<Team> searchByName(String name, Pageable pageable) {
        return teamRepository.findByNameTeamContainingIgnoreCase(name, pageable);
    }
    
    // Get pending invites for current player
    public List<TeamInvite> getMyPendingInvites() {
        Player currentPlayer = userContextService.getCurrentPlayer();
        return teamInviteRepository.findByInvitedPlayerAndStatus(currentPlayer, InviteStatus.PENDING);
    }
    
    // Get pending invites for team (only team leader can see)
    public List<TeamInvite> getTeamPendingInvites(Long teamId) {
        Player currentPlayer = userContextService.getCurrentPlayer();
        
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));
        
        // Validate that current player is the team leader
        if (!team.isLeader(currentPlayer)) {
            throw new BusinessException("Only team leader can view team invites");
        }
        
        return teamInviteRepository.findPendingInvitesByTeamId(teamId);
    }
    
    // Helper method to check mutual following
    private boolean areMutuallyFollowing(Player player1, Player player2) {
        // Check if player1 follows player2 AND player2 follows player1
        boolean player1FollowsPlayer2 = player1.getFollowing() != null && 
                player1.getFollowing().stream().anyMatch(p -> p.getId().equals(player2.getId()));
        
        boolean player2FollowsPlayer1 = player2.getFollowing() != null && 
                player2.getFollowing().stream().anyMatch(p -> p.getId().equals(player1.getId()));
        
        return player1FollowsPlayer2 && player2FollowsPlayer1;
    }
    
    // Convert Team entity to TeamResponse DTO
    public TeamResponse convertToResponse(Team team) {
        TeamResponse response = new TeamResponse();
        response.setId(team.getId());
        response.setNameTeam(team.getNameTeam());
        response.setCreatedAt(team.getCreatedAt());
        response.setUpdatedAt(team.getUpdatedAt());
        
        // Convert leader
        if (team.getLeader() != null) {
            response.setLeader(convertPlayerToSummary(team.getLeader()));
        }
        
        // Convert players list
        List<Player> teamPlayers = playerRepository.findByTeamsContaining(team);
        response.setPlayers(teamPlayers.stream()
                .map(this::convertPlayerToSummary)
                .collect(Collectors.toList()));
        
        response.setPlayerCount(teamPlayers.size());
        
        return response;
    }
    
    // Convert Team entity to TeamSummaryResponse DTO
    public TeamSummaryResponse convertToSummaryResponse(Team team) {
        TeamSummaryResponse response = new TeamSummaryResponse();
        response.setId(team.getId());
        response.setNameTeam(team.getNameTeam());
        
        if (team.getLeader() != null) {
            response.setLeaderUsername(team.getLeader().getRealUsername());
        }
        
        // Count players in team
        long playerCount = playerRepository.countByTeamId(team.getId());
        response.setPlayerCount((int) playerCount);
        
        return response;
    }
    
    // Convert TeamInvite entity to TeamInviteResponse DTO
    public TeamInviteResponse convertInviteToResponse(TeamInvite invite) {
        TeamInviteResponse response = new TeamInviteResponse();
        response.setId(invite.getId());
        response.setStatus(invite.getStatus().name());
        response.setCreatedAt(invite.getCreatedAt());
        response.setUpdatedAt(invite.getUpdatedAt());
        
        // Convert team
        if (invite.getTeam() != null) {
            response.setTeam(convertToSummaryResponse(invite.getTeam()));
        }
        
        // Convert inviter
        if (invite.getInviter() != null) {
            response.setInviter(convertPlayerToSummary(invite.getInviter()));
        }
        
        // Convert invited player
        if (invite.getInvitedPlayer() != null) {
            response.setInvitedPlayer(convertPlayerToSummary(invite.getInvitedPlayer()));
        }
        
        return response;
    }
    
    // Helper method to convert Player to PlayerSummaryResponse
    private PlayerSummaryResponse convertPlayerToSummary(Player player) {
        PlayerSummaryResponse response = new PlayerSummaryResponse();
        response.setId(player.getId());
        response.setUsername(player.getRealUsername());
        response.setName(player.getName());
        response.setEmail(player.getEmail());
        response.setBio(player.getBio());
        response.setProfilePhotoUrl(player.getProfilePhotoUrl());
        response.setFollowersCount(player.getFollowers() != null ? player.getFollowers().size() : 0);
        response.setFollowingCount(player.getFollowing() != null ? player.getFollowing().size() : 0);
        response.setGamesPlayed(player.getGamesPlayed());
        
        return response;
    }
}
