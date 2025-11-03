package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.response.GameSpectatorResponse;
import com.fiap.projects.apipassabola.entity.Game;
import com.fiap.projects.apipassabola.entity.GameSpectator;
import com.fiap.projects.apipassabola.entity.Player;
import com.fiap.projects.apipassabola.entity.Spectator;
import com.fiap.projects.apipassabola.entity.UserType;
import com.fiap.projects.apipassabola.exception.BusinessException;
import com.fiap.projects.apipassabola.exception.ResourceNotFoundException;
import com.fiap.projects.apipassabola.repository.GameRepository;
import com.fiap.projects.apipassabola.repository.GameSpectatorRepository;
import com.fiap.projects.apipassabola.repository.PlayerRepository;
import com.fiap.projects.apipassabola.repository.SpectatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GameSpectatorService {
    
    private final GameSpectatorRepository gameSpectatorRepository;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final SpectatorRepository spectatorRepository;
    private final UserContextService userContextService;
    
    /**
     * Universal method - Player or Spectator joins a game to watch
     */
    public GameSpectatorResponse joinGame(Long gameId) {
        // Get current user context
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        // Validate that only Players and Spectators can watch games
        if (currentUser.getUserType() != UserType.PLAYER && currentUser.getUserType() != UserType.SPECTATOR) {
            throw new BusinessException("Only players and spectators can watch games");
        }
        
        Long watcherId = currentUser.getUserId();
        UserType watcherType = currentUser.getUserType();
        
        // Validate game exists
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game", "id", gameId));
        
        // Validate game accepts spectators
        if (!game.getHasSpectators()) {
            throw new BusinessException("This game does not accept watchers");
        }
        
        // Validate game is FRIENDLY or CHAMPIONSHIP
        if (!game.isFriendlyOrChampionship()) {
            throw new BusinessException("Only FRIENDLY and CHAMPIONSHIP games accept watchers");
        }
        
        // Check if already subscribed
        if (gameSpectatorRepository.existsByGameIdAndWatcherIdAndWatcherType(gameId, watcherId, watcherType)) {
            throw new BusinessException("You are already subscribed to this game");
        }
        
        // Check if game has reached maximum spectators
        long currentSpectatorCount = gameSpectatorRepository.countConfirmedSpectatorsByGame(gameId);
        if (game.hasReachedMaxSpectators((int) currentSpectatorCount)) {
            throw new BusinessException("Game has reached maximum number of watchers (" + game.getMaxSpectators() + ")");
        }
        
        // Get watcher information based on type
        String watcherUsername;
        String watcherName;
        
        if (watcherType == UserType.PLAYER) {
            Player player = playerRepository.findById(watcherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Player", "id", watcherId));
            watcherUsername = player.getRealUsername();
            watcherName = player.getName();
        } else {
            Spectator spectator = spectatorRepository.findById(watcherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Spectator", "id", watcherId));
            watcherUsername = spectator.getRealUsername();
            watcherName = spectator.getName();
        }
        
        // Create subscription with universal fields
        GameSpectator gameSpectator = new GameSpectator();
        gameSpectator.setGame(game);
        gameSpectator.setWatcherId(watcherId);
        gameSpectator.setWatcherUsername(watcherUsername);
        gameSpectator.setWatcherName(watcherName);
        gameSpectator.setWatcherType(watcherType);
        gameSpectator.setStatus(GameSpectator.SpectatorStatus.CONFIRMED);
        
        GameSpectator saved = gameSpectatorRepository.save(gameSpectator);
        
        return convertToResponse(saved);
    }
    
    /**
     * Universal method - Player or Spectator leaves a game
     */
    public void leaveGame(Long gameId) {
        // Get current user context
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        // Validate that only Players and Spectators can leave
        if (currentUser.getUserType() != UserType.PLAYER && currentUser.getUserType() != UserType.SPECTATOR) {
            throw new BusinessException("Only players and spectators can leave games");
        }
        
        Long watcherId = currentUser.getUserId();
        UserType watcherType = currentUser.getUserType();
        
        // Find subscription
        GameSpectator gameSpectator = gameSpectatorRepository
                .findByGameIdAndWatcherIdAndWatcherType(gameId, watcherId, watcherType)
                .orElseThrow(() -> new BusinessException("You are not subscribed to this game"));
        
        // Delete subscription
        gameSpectatorRepository.delete(gameSpectator);
    }
    
    /**
     * Get all spectators for a game
     */
    public List<GameSpectatorResponse> getSpectatorsByGame(Long gameId) {
        // Validate game exists
        if (!gameRepository.existsById(gameId)) {
            throw new ResourceNotFoundException("Game", "id", gameId);
        }
        
        List<GameSpectator> spectators = gameSpectatorRepository.findConfirmedSpectatorsByGame(gameId);
        return spectators.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Universal method - Get all games a watcher (Player or Spectator) is subscribed to
     */
    public Page<GameSpectatorResponse> getMySubscribedGames(Pageable pageable) {
        // Get current user context
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        if (currentUser.getUserType() != UserType.PLAYER && currentUser.getUserType() != UserType.SPECTATOR) {
            throw new BusinessException("Only players and spectators can view their subscribed games");
        }
        
        Long watcherId = currentUser.getUserId();
        UserType watcherType = currentUser.getUserType();
        
        Page<GameSpectator> subscriptions = gameSpectatorRepository
                .findByWatcherIdAndWatcherType(watcherId, watcherType, pageable);
        return subscriptions.map(this::convertToResponse);
    }
    
    /**
     * Universal method - Check if current watcher (Player or Spectator) is subscribed to a game
     */
    public boolean isSubscribed(Long gameId) {
        try {
            UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
            
            if (currentUser.getUserType() != UserType.PLAYER && currentUser.getUserType() != UserType.SPECTATOR) {
                return false;
            }
            
            return gameSpectatorRepository.existsByGameIdAndWatcherIdAndWatcherType(
                    gameId, currentUser.getUserId(), currentUser.getUserType());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get count of confirmed spectators for a game
     */
    public long getConfirmedSpectatorCount(Long gameId) {
        return gameSpectatorRepository.countConfirmedSpectatorsByGame(gameId);
    }
    
    // Conversion method - Universal for Players and Spectators
    private GameSpectatorResponse convertToResponse(GameSpectator gameSpectator) {
        GameSpectatorResponse response = new GameSpectatorResponse();
        response.setId(gameSpectator.getId());
        response.setGameId(gameSpectator.getGame().getId());
        response.setGameName(gameSpectator.getGame().getGameName());
        
        // Use universal watcher fields
        response.setWatcherId(gameSpectator.getWatcherId());
        response.setWatcherUsername(gameSpectator.getWatcherUsername());
        response.setWatcherName(gameSpectator.getWatcherName());
        response.setWatcherType(gameSpectator.getWatcherType());
        
        // Legacy fields for backward compatibility
        response.setSpectatorId(gameSpectator.getWatcherId());
        response.setSpectatorUsername(gameSpectator.getWatcherUsername());
        response.setSpectatorName(gameSpectator.getWatcherName());
        
        response.setStatus(gameSpectator.getStatus());
        response.setJoinedAt(gameSpectator.getJoinedAt());
        response.setCreatedAt(gameSpectator.getCreatedAt());
        return response;
    }
}
