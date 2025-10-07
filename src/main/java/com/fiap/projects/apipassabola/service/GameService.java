package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.request.*;
import com.fiap.projects.apipassabola.dto.response.GameResponse;
import com.fiap.projects.apipassabola.dto.response.GameParticipantResponse;
import com.fiap.projects.apipassabola.dto.response.OrganizationSummaryResponse;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class GameService {
    
    private final GameRepository gameRepository;
    private final OrganizationRepository organizationRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final OrganizationService organizationService;
    private final GameParticipantService gameParticipantService;
    private final UserContextService userContextService;
    private final GameSpectatorRepository gameSpectatorRepository;
    
    public Page<GameResponse> findAll(Pageable pageable) {
        return gameRepository.findAll(pageable).map(this::convertToResponse);
    }
    
    public GameResponse findById(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game", "id", id));
        return convertToResponse(game);
    }
    
    public Page<GameResponse> findByOrganization(Long organizationId, Pageable pageable) {
        return gameRepository.findByHomeTeamIdOrAwayTeamId(organizationId, organizationId, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<GameResponse> findByStatus(Game.GameStatus status, Pageable pageable) {
        return gameRepository.findByStatus(status, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<GameResponse> findByChampionship(String championship, Pageable pageable) {
        return gameRepository.findByChampionshipContainingIgnoreCase(championship, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<GameResponse> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return gameRepository.findByGameDateBetween(startDate, endDate, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<GameResponse> findByGameType(GameType gameType, Pageable pageable) {
        return gameRepository.findByGameType(gameType, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<GameResponse> findByHost(Long hostId, Pageable pageable) {
        // Find games where the host is the specified user (for friendly and championship games)
        return gameRepository.findByGameTypeAndHostId(GameType.FRIENDLY, hostId, pageable)
                .map(this::convertToResponse);
    }
    
    // Methods for creating different types of games
    public GameResponse createFriendlyGame(FriendlyGameRequest request) {
        // Get current user context
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        // Validate that only players can create friendly games
        if (currentUser.getUserType() != UserType.PLAYER) {
            throw new BusinessException("Only players can create friendly games");
        }
        
        // Get player from database to extract username and validate existence
        Player player = playerRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", currentUser.getUserId()));
        
        // Validate player limits
        if (request.getMinPlayers() != null && request.getMaxPlayers() != null) {
            if (request.getMinPlayers() > request.getMaxPlayers()) {
                throw new BusinessException("Minimum players cannot be greater than maximum players");
            }
            if (request.getMinPlayers() % 2 != 0) {
                throw new BusinessException("Minimum players must be an even number for balanced teams");
            }
            if (request.getMaxPlayers() % 2 != 0) {
                throw new BusinessException("Maximum players must be an even number for balanced teams");
            }
        }
        
        Game game = new Game();
        game.setGameType(GameType.FRIENDLY);
        game.setGameName(request.getGameName());
        // Automatically set host information from authenticated user
        game.setHostUsername(player.getRealUsername());
        game.setHostId(player.getId());
        game.setGameDate(request.getGameDate());
        game.setVenue(request.getVenue());
        game.setDescription(request.getDescription());
        
        // Set game configuration
        game.setHasSpectators(request.getHasSpectators());
        game.setMinPlayers(request.getMinPlayers() != null ? request.getMinPlayers() : 6);
        game.setMaxPlayers(request.getMaxPlayers() != null ? request.getMaxPlayers() : 22);
        
        // Validate and set max spectators
        if (request.getHasSpectators()) {
            // If maxSpectators is not provided, default to 5 (minimum)
            Integer maxSpectators = request.getMaxSpectators() != null ? request.getMaxSpectators() : 5;
            
            if (maxSpectators < 5) {
                throw new BusinessException("Maximum spectators must be at least 5 when spectators are enabled");
            }
            game.setMaxSpectators(maxSpectators);
        } else {
            game.setMaxSpectators(0);
        }
        
        game.setStatus(Game.GameStatus.SCHEDULED);
        game.setHomeGoals(0);
        game.setAwayGoals(0);
        
        Game savedGame = gameRepository.save(game);
        return convertToResponse(savedGame);
    }
    
    public GameResponse createChampionshipGame(ChampionshipGameRequest request) {
        // Get current user context
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        // Validate that only players can create championship games
        if (currentUser.getUserType() != UserType.PLAYER) {
            throw new BusinessException("Only players can create championship games");
        }
        
        // Get player from database to extract username and validate existence
        Player player = playerRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", currentUser.getUserId()));
        
        // Validate player limits
        if (request.getMinPlayers() != null && request.getMaxPlayers() != null) {
            if (request.getMinPlayers() > request.getMaxPlayers()) {
                throw new BusinessException("Minimum players cannot be greater than maximum players");
            }
            if (request.getMinPlayers() % 2 != 0) {
                throw new BusinessException("Minimum players must be an even number for balanced teams");
            }
            if (request.getMaxPlayers() % 2 != 0) {
                throw new BusinessException("Maximum players must be an even number for balanced teams");
            }
        }
        
        Game game = new Game();
        game.setGameType(GameType.CHAMPIONSHIP);
        game.setGameName(request.getGameName());
        // Automatically set host information from authenticated user
        game.setHostUsername(player.getRealUsername());
        game.setHostId(player.getId());
        game.setGameDate(request.getGameDate());
        game.setVenue(request.getVenue());
        game.setDescription(request.getDescription());
        
        // Set game configuration
        game.setHasSpectators(request.getHasSpectators());
        game.setMinPlayers(request.getMinPlayers() != null ? request.getMinPlayers() : 6);
        game.setMaxPlayers(request.getMaxPlayers() != null ? request.getMaxPlayers() : 22);
        
        // Validate and set max spectators
        if (request.getHasSpectators()) {
            // If maxSpectators is not provided, default to 5 (minimum)
            Integer maxSpectators = request.getMaxSpectators() != null ? request.getMaxSpectators() : 5;
            
            if (maxSpectators < 5) {
                throw new BusinessException("Maximum spectators must be at least 5 when spectators are enabled");
            }
            game.setMaxSpectators(maxSpectators);
        } else {
            game.setMaxSpectators(0);
        }
        
        game.setStatus(Game.GameStatus.SCHEDULED);
        game.setHomeGoals(0);
        game.setAwayGoals(0);
        
        Game savedGame = gameRepository.save(game);
        return convertToResponse(savedGame);
    }
    
    public GameResponse createCupGame(CupGameRequest request) {
        // Get current user context
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        // Validate that only organizations can create cup games
        if (currentUser.getUserType() != UserType.ORGANIZATION) {
            throw new BusinessException("Only organizations can create cup games");
        }
        
        if (request.getHomeTeamId().equals(request.getAwayTeamId())) {
            throw new BusinessException("Home team and away team cannot be the same");
        }
        
        Organization homeTeam = organizationRepository.findById(request.getHomeTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getHomeTeamId()));
        Organization awayTeam = organizationRepository.findById(request.getAwayTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getAwayTeamId()));
        
        Game game = new Game();
        game.setGameType(GameType.CUP);
        game.setHomeTeam(homeTeam);
        game.setAwayTeam(awayTeam);
        game.setGameDate(request.getGameDate());
        game.setVenue(request.getVenue());
        game.setChampionship(request.getChampionship());
        game.setRound(request.getRound());
        game.setStatus(Game.GameStatus.SCHEDULED);
        game.setHomeGoals(0);
        game.setAwayGoals(0);
        game.setHostId(currentUser.getUserId());
        
        Game savedGame = gameRepository.save(game);
        return convertToResponse(savedGame);
    }
    
    public GameResponse updateFriendlyGame(Long id, FriendlyGameUpdateRequest request) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game", "id", id));
        
        // Validate game type
        if (game.getGameType() != GameType.FRIENDLY) {
            throw new BusinessException("Game is not a friendly game");
        }
        
        // Validate ownership - only the host can update friendly games
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        if (!game.getHostId().equals(currentUser.getUserId()) || currentUser.getUserType() != UserType.PLAYER) {
            throw new BusinessException("Only the game host can update this friendly game");
        }
        
        // Update friendly game fields
        game.setGameName(request.getGameName());
        game.setGameDate(request.getGameDate());
        game.setVenue(request.getVenue());
        game.setDescription(request.getDescription());
        game.setHomeGoals(request.getHomeGoals());
        game.setAwayGoals(request.getAwayGoals());
        game.setStatus(request.getStatus());
        game.setNotes(request.getNotes());
        
        Game savedGame = gameRepository.save(game);
        return convertToResponse(savedGame);
    }
    
    public GameResponse updateChampionshipGame(Long id, ChampionshipGameUpdateRequest request) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game", "id", id));
        
        // Validate game type
        if (game.getGameType() != GameType.CHAMPIONSHIP) {
            throw new BusinessException("Game is not a championship game");
        }
        
        // Validate ownership - only the host can update championship games
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        if (!game.getHostId().equals(currentUser.getUserId()) || currentUser.getUserType() != UserType.PLAYER) {
            throw new BusinessException("Only the game host can update this championship game");
        }
        
        // Update championship game fields
        game.setGameName(request.getGameName());
        game.setGameDate(request.getGameDate());
        game.setVenue(request.getVenue());
        game.setDescription(request.getDescription());
        game.setHomeGoals(request.getHomeGoals());
        game.setAwayGoals(request.getAwayGoals());
        game.setStatus(request.getStatus());
        game.setNotes(request.getNotes());
        
        Game savedGame = gameRepository.save(game);
        return convertToResponse(savedGame);
    }
    
    public GameResponse updateCupGame(Long id, CupGameUpdateRequest request) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game", "id", id));
        
        // Validate game type
        if (game.getGameType() != GameType.CUP) {
            throw new BusinessException("Game is not a cup game");
        }
        
        // Validate ownership - only the creator organization can update cup games
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        if (!game.getHostId().equals(currentUser.getUserId()) || currentUser.getUserType() != UserType.ORGANIZATION) {
            throw new BusinessException("Only the game creator organization can update this cup game");
        }
        
        // Validate team IDs are different
        if (request.getHomeTeamId().equals(request.getAwayTeamId())) {
            throw new BusinessException("Home team and away team cannot be the same");
        }
        
        // Update teams if changed
        if (!request.getHomeTeamId().equals(game.getHomeTeam().getId())) {
            Organization homeTeam = organizationRepository.findById(request.getHomeTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getHomeTeamId()));
            game.setHomeTeam(homeTeam);
        }
        
        if (!request.getAwayTeamId().equals(game.getAwayTeam().getId())) {
            Organization awayTeam = organizationRepository.findById(request.getAwayTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getAwayTeamId()));
            game.setAwayTeam(awayTeam);
        }
        
        // Update cup game fields
        game.setGameDate(request.getGameDate());
        game.setVenue(request.getVenue());
        game.setChampionship(request.getChampionship());
        game.setRound(request.getRound());
        game.setHomeGoals(request.getHomeGoals());
        game.setAwayGoals(request.getAwayGoals());
        game.setStatus(request.getStatus());
        game.setNotes(request.getNotes());
        
        Game savedGame = gameRepository.save(game);
        return convertToResponse(savedGame);
    }
    
    @Deprecated
    public GameResponse update(Long id, GameRequest request) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game", "id", id));
        
        if (request.getHomeTeamId() != null && !request.getHomeTeamId().equals(game.getHomeTeam().getId())) {
            Organization homeTeam = organizationRepository.findById(request.getHomeTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getHomeTeamId()));
            game.setHomeTeam(homeTeam);
        }
        
        if (request.getAwayTeamId() != null && !request.getAwayTeamId().equals(game.getAwayTeam().getId())) {
            Organization awayTeam = organizationRepository.findById(request.getAwayTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getAwayTeamId()));
            game.setAwayTeam(awayTeam);
        }
        
        if (game.getHomeTeam().getId().equals(game.getAwayTeam().getId())) {
            throw new BusinessException("Home team and away team cannot be the same");
        }
        
        game.setGameDate(request.getGameDate());
        game.setVenue(request.getVenue());
        game.setHomeGoals(request.getHomeGoals());
        game.setAwayGoals(request.getAwayGoals());
        game.setStatus(request.getStatus());
        game.setChampionship(request.getChampionship());
        game.setRound(request.getRound());
        game.setNotes(request.getNotes());
        
        Game savedGame = gameRepository.save(game);
        return convertToResponse(savedGame);
    }
    
    public void delete(Long id) {
        if (!gameRepository.existsById(id)) {
            throw new ResourceNotFoundException("Game", "id", id);
        }
        gameRepository.deleteById(id);
    }
    
    public GameResponse updateScore(Long id, Integer homeGoals, Integer awayGoals) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game", "id", id));
        
        if (homeGoals != null && homeGoals < 0) {
            throw new BusinessException("Home goals cannot be negative");
        }
        if (awayGoals != null && awayGoals < 0) {
            throw new BusinessException("Away goals cannot be negative");
        }
        
        game.setHomeGoals(homeGoals);
        game.setAwayGoals(awayGoals);
        
        // Auto-update status based on current time and score
        if (game.getGameDate().isBefore(LocalDateTime.now()) && 
            homeGoals != null && awayGoals != null) {
            game.setStatus(Game.GameStatus.FINISHED);
        }
        
        Game savedGame = gameRepository.save(game);
        return convertToResponse(savedGame);
    }
    
    public Page<GameResponse> findFriendlyAndChampionshipGames(Pageable pageable) {
        return gameRepository.findAll(pageable)
                .map(this::convertToResponse)
                .map(response -> response.getGameType() == GameType.FRIENDLY || response.getGameType() == GameType.CHAMPIONSHIP ? response : null)
                .map(response -> response);
    }
    
    public Page<GameResponse> findGamesByHost(Long hostId, Pageable pageable) {
        return gameRepository.findByGameTypeAndHostId(GameType.FRIENDLY, hostId, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<GameResponse> searchFriendlyAndChampionshipGames(String gameName, Pageable pageable) {
        return gameRepository.findFriendlyAndChampionshipByGameNameContaining(gameName, pageable)
                .map(this::convertToResponse);
    }
    
    private GameResponse convertToResponse(Game game) {
        GameResponse response = new GameResponse();
        response.setId(game.getId());
        response.setGameType(game.getGameType());
        
        // Set fields based on game type
        if (game.isFriendlyOrChampionship()) {
            response.setGameName(game.getGameName());
            response.setHostUsername(game.getHostUsername());
            response.setHostId(game.getHostId());
            response.setDescription(game.getDescription());
            
            // Get participants for each team side
            List<GameParticipantResponse> team1Players = gameParticipantService.getGameParticipantsByTeamSide(game.getId(), 1);
            List<GameParticipantResponse> team2Players = gameParticipantService.getGameParticipantsByTeamSide(game.getId(), 2);
            response.setTeam1Players(team1Players);
            response.setTeam2Players(team2Players);
            
            // Set game configuration
            response.setHasSpectators(game.getHasSpectators());
            response.setMinPlayers(game.getMinPlayers());
            response.setMaxPlayers(game.getMaxPlayers());
            response.setMaxSpectators(game.getMaxSpectators());
            
            // Get current spectator count
            long spectatorCount = gameSpectatorRepository.countConfirmedSpectatorsByGame(game.getId());
            response.setCurrentSpectatorCount((int) spectatorCount);
            
            // Calculate team counts and balance
            int team1Count = team1Players.size();
            int team2Count = team2Players.size();
            int totalPlayers = team1Count + team2Count;
            
            response.setTeam1Count(team1Count);
            response.setTeam2Count(team2Count);
            response.setCurrentPlayerCount(totalPlayers);
            response.setIsTeamsBalanced(game.areTeamsBalanced(team1Count, team2Count));
            
            // Check if game can start (minimum players + balanced teams)
            boolean hasMinimumPlayers = game.hasMinimumPlayers(totalPlayers);
            boolean teamsBalanced = game.areTeamsBalanced(team1Count, team2Count);
            response.setCanStart(hasMinimumPlayers && teamsBalanced);
        } else if (game.isCupGame()) {
            if (game.getHomeTeam() != null) {
                response.setHomeTeam(organizationService.convertToSummaryResponse(game.getHomeTeam()));
            }
            if (game.getAwayTeam() != null) {
                response.setAwayTeam(organizationService.convertToSummaryResponse(game.getAwayTeam()));
            }
            response.setChampionship(game.getChampionship());
            response.setRound(game.getRound());
        }
        
        // Common fields
        response.setGameDate(game.getGameDate());
        response.setVenue(game.getVenue());
        response.setHomeGoals(game.getHomeGoals());
        response.setAwayGoals(game.getAwayGoals());
        response.setStatus(game.getStatus());
        response.setNotes(game.getNotes());
        response.setResult(game.getResult());
        response.setDraw(game.isDraw());
        response.setCreatedAt(game.getCreatedAt());
        response.setUpdatedAt(game.getUpdatedAt());
        
        // Set winner information based on game type
        if (game.isCupGame() && game.getWinner() != null) {
            // Cup game with different organizations - return organization winner
            response.setWinner(organizationService.convertToSummaryResponse(game.getWinner()));
        } else {
            // Friendly, Championship, or Cup game with same organization - return team side winner
            response.setWinningTeamSide(game.getWinningTeamSide());
        }
        
        return response;
    }
}
