package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.request.*;
import com.fiap.projects.apipassabola.dto.response.GameResponse;
import com.fiap.projects.apipassabola.dto.response.GameParticipantResponse;
import com.fiap.projects.apipassabola.dto.response.OrganizationSummaryResponse;
import com.fiap.projects.apipassabola.entity.*;
import com.fiap.projects.apipassabola.exception.BusinessException;
import com.fiap.projects.apipassabola.exception.ResourceNotFoundException;
import com.fiap.projects.apipassabola.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class GameService {
    
    private final GameRepository gameRepository;
    private final OrganizationRepository organizationRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final OrganizationService organizationService;
    private final GameParticipantService gameParticipantService;
    private final UserContextService userContextService;
    private final GameSpectatorRepository gameSpectatorRepository;
    private final RankingPointsService rankingPointsService;
    private final GoalRepository goalRepository;
    
    private final TournamentService tournamentService; // Lazy injection para evitar dependência circular
    
    // Construtor customizado para injeção lazy
    public GameService(GameRepository gameRepository,
                      OrganizationRepository organizationRepository,
                      PlayerRepository playerRepository,
                      TeamRepository teamRepository,
                      OrganizationService organizationService,
                      GameParticipantService gameParticipantService,
                      UserContextService userContextService,
                      GameSpectatorRepository gameSpectatorRepository,
                      RankingPointsService rankingPointsService,
                      GoalRepository goalRepository,
                      @org.springframework.context.annotation.Lazy TournamentService tournamentService) {
        this.gameRepository = gameRepository;
        this.organizationRepository = organizationRepository;
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        this.organizationService = organizationService;
        this.gameParticipantService = gameParticipantService;
        this.userContextService = userContextService;
        this.gameSpectatorRepository = gameSpectatorRepository;
        this.rankingPointsService = rankingPointsService;
        this.goalRepository = goalRepository;
        this.tournamentService = tournamentService;
    }
    
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
    
    /**
     * Cria um jogo de torneio automaticamente
     * Usado pelo sistema de torneios para criar jogos das partidas do bracket
     */
    public Game createTournamentGame(Team team1, Team team2, String venue, LocalDateTime gameDate, 
                                     String tournamentName, String round, Long creatorId) {
        // Para jogos de torneio, usamos o tipo CHAMPIONSHIP
        // Isso permite que o sistema de ranking e pontos funcione corretamente
        Game game = new Game();
        game.setGameType(GameType.CHAMPIONSHIP);
        game.setGameName(tournamentName + " - " + round);
        game.setGameDate(gameDate);
        game.setVenue(venue);
        game.setChampionship(tournamentName);
        game.setRound(round);
        game.setDescription("Partida do torneio " + tournamentName);
        game.setStatus(Game.GameStatus.SCHEDULED);
        game.setHomeGoals(0);
        game.setAwayGoals(0);
        game.setHostId(creatorId);
        
        // Configurações padrão para jogos de torneio
        game.setHasSpectators(true);
        game.setMaxSpectators(100);
        game.setMinPlayers(10); // 5x5
        game.setMaxPlayers(22); // 11x11
        
        return gameRepository.save(game);
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
        
        // Store previous status to check if game just finished
        Game.GameStatus previousStatus = game.getStatus();
        
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
        
        // Friendly games don't count for ranking, so no points distribution
        
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
        
        // Store previous status to check if game just finished
        Game.GameStatus previousStatus = game.getStatus();
        
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
        
        // Distribute ranking points if game just finished (CHAMPIONSHIP counts for ranking)
        if (previousStatus != Game.GameStatus.FINISHED && savedGame.getStatus() == Game.GameStatus.FINISHED) {
            rankingPointsService.distributePointsAfterGame(savedGame);
        }
        
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
        
        // Store previous status to check if game just finished
        Game.GameStatus previousStatus = game.getStatus();
        
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
        
        // Distribute ranking points if game just finished (CUP counts for ranking)
        if (previousStatus != Game.GameStatus.FINISHED && savedGame.getStatus() == Game.GameStatus.FINISHED) {
            rankingPointsService.distributePointsAfterGame(savedGame);
        }
        
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
        
        // Store previous status to check if game just finished
        Game.GameStatus previousStatus = game.getStatus();
        
        game.setHomeGoals(homeGoals);
        game.setAwayGoals(awayGoals);
        
        // Auto-update status based on current time and score
        if (game.getGameDate().isBefore(LocalDateTime.now()) && 
            homeGoals != null && awayGoals != null) {
            game.setStatus(Game.GameStatus.FINISHED);
        }
        
        Game savedGame = gameRepository.save(game);
        
        // Distribute ranking points if game just finished
        if (previousStatus != Game.GameStatus.FINISHED && savedGame.getStatus() == Game.GameStatus.FINISHED) {
            rankingPointsService.distributePointsAfterGame(savedGame);
        }
        
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
    
    /**
     * Finaliza um jogo com placar e gols das jogadoras
     * Apenas o criador do jogo pode finalizá-lo
     */
    public GameResponse finishGame(Long gameId, FinishGameRequest request) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game", "id", gameId));
        
        // Verifica se o jogo já está finalizado
        if (game.getStatus() == Game.GameStatus.FINISHED) {
            throw new BusinessException("Game is already finished");
        }
        
        // Valida permissão: apenas o criador pode finalizar
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        if (!game.getHostId().equals(currentUser.getUserId())) {
            throw new BusinessException("Only the game creator can finish this game");
        }
        
        // Valida que o número de gols bate com a lista de gols
        if (request.getGoals() != null && !request.getGoals().isEmpty()) {
            long team1Goals = request.getGoals().stream()
                    .filter(g -> g.getTeamSide() == 1 && !g.getIsOwnGoal())
                    .count();
            long team2Goals = request.getGoals().stream()
                    .filter(g -> g.getTeamSide() == 2 && !g.getIsOwnGoal())
                    .count();
            
            // Para jogos amistosos/campeonatos: homeGoals = time 1, awayGoals = time 2
            if (game.isFriendlyOrChampionship()) {
                if (team1Goals != request.getHomeGoals() || team2Goals != request.getAwayGoals()) {
                    throw new BusinessException(
                        String.format("Goal count mismatch. Expected: Team1=%d, Team2=%d. Got: Team1=%d, Team2=%d",
                            request.getHomeGoals(), request.getAwayGoals(), team1Goals, team2Goals)
                    );
                }
            }
        }
        
        // Atualiza o placar
        game.setHomeGoals(request.getHomeGoals());
        game.setAwayGoals(request.getAwayGoals());
        game.setStatus(Game.GameStatus.FINISHED);
        if (request.getNotes() != null) {
            game.setNotes(request.getNotes());
        }
        
        Game savedGame = gameRepository.save(game);
        
        // Registra os gols das jogadoras
        if (request.getGoals() != null && !request.getGoals().isEmpty()) {
            for (GoalRequest goalRequest : request.getGoals()) {
                Player player = playerRepository.findById(goalRequest.getPlayerId())
                        .orElseThrow(() -> new ResourceNotFoundException("Player", "id", goalRequest.getPlayerId()));
                
                Goal goal = new Goal();
                goal.setGame(savedGame);
                goal.setPlayer(player);
                goal.setTeamSide(goalRequest.getTeamSide());
                goal.setMinute(goalRequest.getMinute());
                goal.setIsOwnGoal(goalRequest.getIsOwnGoal() != null ? goalRequest.getIsOwnGoal() : false);
                
                goalRepository.save(goal);
            }
        }
        
        // Distribui pontos de ranking (apenas para CHAMPIONSHIP e CUP)
        rankingPointsService.distributePointsAfterGame(savedGame);
        
        // Sincroniza resultado com torneio (se o jogo faz parte de um torneio)
        if (tournamentService != null) {
            try {
                tournamentService.syncGameResultToMatch(savedGame.getId(), 
                    savedGame.getHomeGoals(), savedGame.getAwayGoals());
            } catch (Exception e) {
                // Não falha se não houver torneio associado
                log.debug("Jogo {} não está associado a nenhum torneio", savedGame.getId());
            }
        }
        
        return convertToResponse(savedGame);
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
