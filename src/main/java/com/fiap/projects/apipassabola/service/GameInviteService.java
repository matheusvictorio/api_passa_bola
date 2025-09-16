package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.request.GameInviteRequest;
import com.fiap.projects.apipassabola.dto.response.GameInviteResponse;
import com.fiap.projects.apipassabola.dto.response.OrganizationSummaryResponse;
import com.fiap.projects.apipassabola.dto.response.TeamSummaryResponse;
import com.fiap.projects.apipassabola.entity.*;
import com.fiap.projects.apipassabola.exception.BusinessException;
import com.fiap.projects.apipassabola.exception.ResourceNotFoundException;
import com.fiap.projects.apipassabola.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GameInviteService {
    
    private final GameInviteRepository gameInviteRepository;
    private final GameRepository gameRepository;
    private final OrganizationRepository organizationRepository;
    private final TeamRepository teamRepository;
    private final OrganizationService organizationService;
    private final TeamService teamService;
    private final UserContextService userContextService;
    
    public GameInviteResponse sendInvite(Long organizationId, GameInviteRequest request) {
        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new ResourceNotFoundException("Game", "id", request.getGameId()));
        
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
        
        Team team = teamRepository.findById(request.getInvitedTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", request.getInvitedTeamId()));
        
        // Validate game type is CUP
        if (game.getGameType() != GameType.CUP) {
            throw new BusinessException("Only CUP games support team invitations");
        }
        
        // Check if game is in the future
        if (game.getGameDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Cannot send invite for a game that has already started or finished");
        }
        
        // Check if organization created this game
        if (!game.getHostId().equals(organizationId)) {
            throw new BusinessException("Only the game creator can send invitations");
        }
        
        // Check if team is already invited
        if (gameInviteRepository.existsByGameIdAndInvitedTeamIdAndStatus(
                request.getGameId(), request.getInvitedTeamId(), GameInvite.InviteStatus.PENDING)) {
            throw new BusinessException("Team already has a pending invitation for this game");
        }
        
        // Check if team is already accepted for this game
        if (gameInviteRepository.existsByGameIdAndInvitedTeamIdAndStatus(
                request.getGameId(), request.getInvitedTeamId(), GameInvite.InviteStatus.ACCEPTED)) {
            throw new BusinessException("Team has already accepted an invitation for this game");
        }
        
        // Validate team position
        if (!"HOME".equalsIgnoreCase(request.getTeamPosition()) && 
            !"AWAY".equalsIgnoreCase(request.getTeamPosition())) {
            throw new BusinessException("Team position must be HOME or AWAY");
        }
        
        // Check if position is already taken
        List<GameInvite> acceptedInvites = gameInviteRepository.findAcceptedInvitesForGame(request.getGameId());
        for (GameInvite invite : acceptedInvites) {
            if (request.getTeamPosition().equalsIgnoreCase(invite.getTeamPosition())) {
                throw new BusinessException("The " + request.getTeamPosition() + " position is already taken");
            }
        }
        
        GameInvite invite = new GameInvite();
        invite.setGame(game);
        invite.setInvitingOrganization(organization);
        invite.setInvitedTeam(team);
        invite.setTeamPosition(request.getTeamPosition().toUpperCase());
        invite.setMessage(request.getMessage());
        invite.setStatus(GameInvite.InviteStatus.PENDING);
        
        GameInvite savedInvite = gameInviteRepository.save(invite);
        return convertToResponse(savedInvite);
    }
    
    public GameInviteResponse acceptInvite(Long teamId, Long inviteId) {
        GameInvite invite = gameInviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("GameInvite", "id", inviteId));
        
        // Validate team ownership
        if (!invite.getInvitedTeam().getId().equals(teamId)) {
            throw new BusinessException("Only the invited team can accept this invitation");
        }
        
        // Check if invite can be accepted
        if (!invite.canBeAccepted()) {
            throw new BusinessException("This invitation cannot be accepted (expired or already responded)");
        }
        
        // Accept the invite
        invite.accept();
        
        // Update game with team information
        Game game = invite.getGame();
        if ("HOME".equalsIgnoreCase(invite.getTeamPosition())) {
            // For now, we'll use the team's organization as homeTeam
            // This might need adjustment based on your business logic
            if (invite.getInvitedTeam().getLeader() != null && 
                invite.getInvitedTeam().getLeader().getOrganization() != null) {
                game.setHomeTeam(invite.getInvitedTeam().getLeader().getOrganization());
            }
        } else if ("AWAY".equalsIgnoreCase(invite.getTeamPosition())) {
            if (invite.getInvitedTeam().getLeader() != null && 
                invite.getInvitedTeam().getLeader().getOrganization() != null) {
                game.setAwayTeam(invite.getInvitedTeam().getLeader().getOrganization());
            }
        }
        
        gameRepository.save(game);
        GameInvite savedInvite = gameInviteRepository.save(invite);
        return convertToResponse(savedInvite);
    }
    
    public GameInviteResponse rejectInvite(Long teamId, Long inviteId) {
        GameInvite invite = gameInviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("GameInvite", "id", inviteId));
        
        // Validate team ownership
        if (!invite.getInvitedTeam().getId().equals(teamId)) {
            throw new BusinessException("Only the invited team can reject this invitation");
        }
        
        // Check if invite is pending
        if (!invite.isPending()) {
            throw new BusinessException("This invitation has already been responded to");
        }
        
        invite.reject();
        GameInvite savedInvite = gameInviteRepository.save(invite);
        return convertToResponse(savedInvite);
    }
    
    public void cancelInvite(Long organizationId, Long inviteId) {
        GameInvite invite = gameInviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("GameInvite", "id", inviteId));
        
        // Validate organization ownership
        if (!invite.getInvitingOrganization().getId().equals(organizationId)) {
            throw new BusinessException("Only the inviting organization can cancel this invitation");
        }
        
        // Check if invite can be cancelled
        if (!invite.canBeCancelled()) {
            throw new BusinessException("This invitation cannot be cancelled");
        }
        
        invite.cancel();
        gameInviteRepository.save(invite);
    }
    
    public Page<GameInviteResponse> getGameInvites(Long gameId, Pageable pageable) {
        return gameInviteRepository.findByGameId(gameId, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<GameInviteResponse> getTeamInvites(Long teamId, Pageable pageable) {
        return gameInviteRepository.findByInvitedTeamId(teamId, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<GameInviteResponse> getOrganizationInvites(Long organizationId, Pageable pageable) {
        return gameInviteRepository.findByInvitingOrganizationId(organizationId, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<GameInviteResponse> getPendingTeamInvites(Long teamId, Pageable pageable) {
        return gameInviteRepository.findByInvitedTeamIdAndStatus(teamId, GameInvite.InviteStatus.PENDING, pageable)
                .map(this::convertToResponse);
    }
    
    public void expireOldInvites() {
        List<GameInvite> expiredInvites = gameInviteRepository.findExpiredInvites(LocalDateTime.now());
        for (GameInvite invite : expiredInvites) {
            invite.setStatus(GameInvite.InviteStatus.EXPIRED);
        }
        gameInviteRepository.saveAll(expiredInvites);
    }
    
    // Additional methods for controller support
    public List<GameInviteResponse> getInvitesByGame(Long gameId) {
        return gameInviteRepository.findByGameId(gameId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public Page<GameInviteResponse> getInvitesByOrganization(Long organizationId, Pageable pageable) {
        return gameInviteRepository.findByInvitingOrganizationId(organizationId, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<GameInviteResponse> getInvitesByTeam(Long teamId, Pageable pageable) {
        return gameInviteRepository.findByInvitedTeamId(teamId, pageable)
                .map(this::convertToResponse);
    }
    
    public List<GameInviteResponse> getPendingInvitesForCurrentOrganization() {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        return gameInviteRepository.findByInvitingOrganizationIdAndStatus(currentUser.getUserId(), GameInvite.InviteStatus.PENDING)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public Page<GameInviteResponse> getSentInvitesByCurrentOrganization(Pageable pageable) {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        return gameInviteRepository.findByInvitingOrganizationId(currentUser.getUserId(), pageable)
                .map(this::convertToResponse);
    }
    
    private GameInviteResponse convertToResponse(GameInvite invite) {
        GameInviteResponse response = new GameInviteResponse();
        response.setId(invite.getId());
        response.setGameId(invite.getGame().getId());
        
        // Set game name based on game type
        if (invite.getGame().getGameType() == GameType.CUP) {
            response.setGameName(invite.getGame().getChampionship() + " - " + invite.getGame().getRound());
        } else {
            response.setGameName(invite.getGame().getGameName());
        }
        
        response.setInvitingOrganization(organizationService.convertToSummaryResponse(invite.getInvitingOrganization()));
        response.setInvitedTeam(teamService.convertToSummaryResponse(invite.getInvitedTeam()));
        response.setStatus(invite.getStatus());
        response.setTeamPosition(invite.getTeamPosition());
        response.setMessage(invite.getMessage());
        response.setInvitedAt(invite.getInvitedAt());
        response.setRespondedAt(invite.getRespondedAt());
        response.setCreatedAt(invite.getCreatedAt());
        response.setUpdatedAt(invite.getUpdatedAt());
        return response;
    }
}
