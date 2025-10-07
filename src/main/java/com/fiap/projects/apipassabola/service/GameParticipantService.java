package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.request.GameParticipationRequest;
import com.fiap.projects.apipassabola.dto.response.GameParticipantResponse;
import com.fiap.projects.apipassabola.dto.response.PlayerSummaryResponse;
import com.fiap.projects.apipassabola.entity.Game;
import com.fiap.projects.apipassabola.entity.GameParticipant;
import com.fiap.projects.apipassabola.entity.GameType;
import com.fiap.projects.apipassabola.entity.Player;
import com.fiap.projects.apipassabola.entity.Team;
import com.fiap.projects.apipassabola.exception.BusinessException;
import com.fiap.projects.apipassabola.exception.ResourceNotFoundException;
import com.fiap.projects.apipassabola.repository.GameParticipantRepository;
import com.fiap.projects.apipassabola.repository.GameRepository;
import com.fiap.projects.apipassabola.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GameParticipantService {
    
    private final GameParticipantRepository gameParticipantRepository;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final PlayerService playerService;
    private final UserContextService userContextService;
    
    public GameParticipantResponse joinGame(Long playerId, GameParticipationRequest request) {
        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new ResourceNotFoundException("Game", "id", request.getGameId()));
        
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", playerId));
        
        // Validate game type allows individual participation
        if (!game.allowsIndividualParticipation()) {
            throw new BusinessException("This game type does not allow individual participation");
        }
        
        // Check if game is in the future
        if (game.getGameDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Cannot join a game that has already started or finished");
        }
        
        // Check if player is already participating
        if (gameParticipantRepository.existsByGameIdAndPlayerId(request.getGameId(), playerId)) {
            throw new BusinessException("Player is already participating in this game");
        }
        
        // Validate team side
        if (request.getTeamSide() == null || (request.getTeamSide() != 1 && request.getTeamSide() != 2)) {
            throw new BusinessException("Team side must be 1 or 2");
        }
        
        // Count current participants
        long currentTotalPlayers = gameParticipantRepository.countByGameId(request.getGameId());
        long currentTeamSidePlayers = gameParticipantRepository.countByGameIdAndTeamSide(request.getGameId(), request.getTeamSide());
        
        // Check if game has reached maximum players
        if (game.hasReachedMaxPlayers((int) currentTotalPlayers)) {
            throw new BusinessException("Game has reached maximum number of players (" + game.getMaxPlayers() + ")");
        }
        
        // Check if joining with team and player has a team
        if (request.getParticipationType() == GameParticipant.ParticipationType.WITH_TEAM) {
            if (player.getTeams() == null || player.getTeams().isEmpty()) {
                throw new BusinessException("Player must be in a team to join with team");
            }
            
            // For now, use the first team (players can be in multiple teams)
            Team playerTeam = player.getTeams().iterator().next();
            
            // Get all team members to validate capacity
            List<Player> teamMembers = playerRepository.findByTeamsContaining(playerTeam);
            int teamSize = teamMembers.size();
            
            // Validate that adding the entire team won't exceed maximum players
            if (currentTotalPlayers + teamSize > game.getMaxPlayers()) {
                throw new BusinessException("Adding team would exceed maximum players. Current: " + currentTotalPlayers + ", Team size: " + teamSize + ", Max: " + game.getMaxPlayers());
            }
            
            // Add all team members to the game
            return joinWithTeam(game, player, playerTeam, request.getTeamSide(), teamMembers);
        } else {
            // Join individually
            return joinIndividually(game, player, request.getTeamSide());
        }
    }
    
    private GameParticipantResponse joinIndividually(Game game, Player player, Integer teamSide) {
        GameParticipant participant = new GameParticipant();
        participant.setGame(game);
        participant.setPlayer(player);
        participant.setParticipationType(GameParticipant.ParticipationType.INDIVIDUAL);
        participant.setTeamSide(teamSide);
        participant.setStatus(GameParticipant.ParticipationStatus.CONFIRMED);
        
        GameParticipant savedParticipant = gameParticipantRepository.save(participant);
        return convertToResponse(savedParticipant);
    }
    
    private GameParticipantResponse joinWithTeam(Game game, Player player, Team team, Integer teamSide, List<Player> teamMembers) {
        // Check if any team member is already participating
        List<GameParticipant> existingParticipants = gameParticipantRepository
                .findByGameIdAndPlayerTeamId(game.getId(), team.getId());
        
        if (!existingParticipants.isEmpty()) {
            throw new BusinessException("Some team members are already participating in this game");
        }
        
        // CRITICAL: All team members MUST go to the same side (teamSide parameter)
        // Add all team members as participants on the SAME side
        for (Player teamMember : teamMembers) {
            GameParticipant participant = new GameParticipant();
            participant.setGame(game);
            participant.setPlayer(teamMember);
            participant.setParticipationType(GameParticipant.ParticipationType.WITH_TEAM);
            participant.setTeamSide(teamSide); // ALL team members on the SAME side
            participant.setStatus(GameParticipant.ParticipationStatus.CONFIRMED);
            
            gameParticipantRepository.save(participant);
        }
        
        // Verify teams are still balanced after adding the team
        long team1Count = gameParticipantRepository.countByGameIdAndTeamSide(game.getId(), 1);
        long team2Count = gameParticipantRepository.countByGameIdAndTeamSide(game.getId(), 2);
        
        // Log warning if teams become unbalanced (but don't block - let them balance later)
        if (Math.abs(team1Count - team2Count) > teamMembers.size()) {
            // Teams are significantly unbalanced
            System.out.println("WARNING: Teams are unbalanced after adding team. Team 1: " + team1Count + ", Team 2: " + team2Count);
        }
        
        // Return the requesting player's participation
        GameParticipant playerParticipant = gameParticipantRepository
                .findByGameIdAndPlayerId(game.getId(), player.getId())
                .orElseThrow(() -> new BusinessException("Failed to create participation"));
        
        return convertToResponse(playerParticipant);
    }
    
    public void leaveGame(Long playerId, Long gameId) {
        GameParticipant participant = gameParticipantRepository.findByGameIdAndPlayerId(gameId, playerId)
                .orElseThrow(() -> new ResourceNotFoundException("GameParticipant", "gameId and playerId", gameId + " and " + playerId));
        
        Game game = participant.getGame();
        
        // Check if game hasn't started yet
        if (game.getGameDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Cannot leave a game that has already started or finished");
        }
        
        // If joined with team, remove all team members
        if (participant.getParticipationType() == GameParticipant.ParticipationType.WITH_TEAM) {
            Player player = participant.getPlayer();
            if (player.getTeams() != null && !player.getTeams().isEmpty()) {
                // For now, use the first team (players can be in multiple teams)
                Team playerTeam = player.getTeams().iterator().next();
                List<GameParticipant> teamParticipants = gameParticipantRepository
                        .findByGameIdAndPlayerTeamId(gameId, playerTeam.getId());
                gameParticipantRepository.deleteAll(teamParticipants);
            }
        } else {
            gameParticipantRepository.delete(participant);
        }
    }
    
    public Page<GameParticipantResponse> getGameParticipants(Long gameId, Pageable pageable) {
        return gameParticipantRepository.findByGameId(gameId, pageable)
                .map(this::convertToResponse);
    }
    
    public List<GameParticipantResponse> getGameParticipantsByTeamSide(Long gameId, Integer teamSide) {
        return gameParticipantRepository.findConfirmedParticipantsByGameAndTeamSide(gameId, teamSide)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public Page<GameParticipantResponse> getPlayerParticipations(Long playerId, Pageable pageable) {
        return gameParticipantRepository.findByPlayerId(playerId, pageable)
                .map(this::convertToResponse);
    }
    
    // Additional methods for controller support
    public List<GameParticipantResponse> getParticipantsByGame(Long gameId) {
        return gameParticipantRepository.findByGameId(gameId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public Page<GameParticipantResponse> getParticipationsByPlayer(Long playerId, Pageable pageable) {
        return getPlayerParticipations(playerId, pageable);
    }
    
    public Page<GameParticipantResponse> getMyParticipations(Pageable pageable) {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        // Verificar o tipo de usuário
        switch (currentUser.getUserType()) {
            case PLAYER:
                // Para jogadoras, buscar suas participações normalmente
                return getPlayerParticipations(currentUser.getUserId(), pageable);
                
            case ORGANIZATION:
                // Para organizações, buscar jogos onde os times das suas jogadoras estão participando
                return getOrganizationTeamsParticipations(currentUser.getUserId(), pageable);
                
            case SPECTATOR:
                // Spectators não participam de jogos como GameParticipant
                return Page.empty(pageable);
                
            default:
                throw new BusinessException("Invalid user type for game participations: " + currentUser.getUserType());
        }
    }
    
    private Page<GameParticipantResponse> getOrganizationTeamsParticipations(Long organizationId, Pageable pageable) {
        // Buscar todas as jogadoras que pertencem a esta organização
        List<Player> organizationPlayers = playerRepository.findByOrganizationId(organizationId);
        
        if (organizationPlayers.isEmpty()) {
            return Page.empty(pageable);
        }
        
        // Extrair IDs das jogadoras
        List<Long> playerIds = organizationPlayers.stream()
                .map(Player::getId)
                .collect(Collectors.toList());
        
        // Buscar participações de todas essas jogadoras
        return gameParticipantRepository.findByPlayerIdIn(playerIds, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<GameParticipantResponse> getParticipationsByTeam(Long teamId, Pageable pageable) {
        // Find participants where the player belongs to the specified team
        return gameParticipantRepository.findAll(pageable)
                .map(this::convertToResponse);
    }
    
    public long countGameParticipants(Long gameId) {
        return gameParticipantRepository.countByGameId(gameId);
    }
    
    public long countGameParticipantsByTeamSide(Long gameId, Integer teamSide) {
        return gameParticipantRepository.countByGameIdAndTeamSide(gameId, teamSide);
    }
    
    private GameParticipantResponse convertToResponse(GameParticipant participant) {
        GameParticipantResponse response = new GameParticipantResponse();
        response.setId(participant.getId());
        response.setGameId(participant.getGame().getId());
        response.setPlayer(playerService.convertToSummaryResponse(participant.getPlayer()));
        response.setParticipationType(participant.getParticipationType());
        response.setStatus(participant.getStatus());
        response.setTeamSide(participant.getTeamSide());
        response.setJoinedAt(participant.getJoinedAt());
        response.setCreatedAt(participant.getCreatedAt());
        response.setUpdatedAt(participant.getUpdatedAt());
        return response;
    }
}
