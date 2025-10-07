package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.response.GameSpectatorResponse;
import com.fiap.projects.apipassabola.entity.Game;
import com.fiap.projects.apipassabola.entity.GameSpectator;
import com.fiap.projects.apipassabola.entity.Spectator;
import com.fiap.projects.apipassabola.entity.UserType;
import com.fiap.projects.apipassabola.exception.BusinessException;
import com.fiap.projects.apipassabola.exception.ResourceNotFoundException;
import com.fiap.projects.apipassabola.repository.GameRepository;
import com.fiap.projects.apipassabola.repository.GameSpectatorRepository;
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
    private final SpectatorRepository spectatorRepository;
    private final UserContextService userContextService;
    
    /**
     * Spectator joins a game to watch
     */
    public GameSpectatorResponse joinGame(Long gameId) {
        // Get current user context
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        // Validate that only spectators can join as spectators
        if (currentUser.getUserType() != UserType.SPECTATOR) {
            throw new BusinessException("Only spectators can join games as spectators");
        }
        
        Long spectatorId = currentUser.getUserId();
        
        // Validate game exists
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game", "id", gameId));
        
        // Validate game accepts spectators
        if (!game.getHasSpectators()) {
            throw new BusinessException("This game does not accept spectators");
        }
        
        // Validate game is FRIENDLY or CHAMPIONSHIP
        if (!game.isFriendlyOrChampionship()) {
            throw new BusinessException("Only FRIENDLY and CHAMPIONSHIP games accept spectators");
        }
        
        // Validate spectator exists
        Spectator spectator = spectatorRepository.findById(spectatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Spectator", "id", spectatorId));
        
        // Check if already subscribed
        if (gameSpectatorRepository.existsByGameIdAndSpectatorId(gameId, spectatorId)) {
            throw new BusinessException("You are already subscribed to this game");
        }
        
        // Check if game has reached maximum spectators
        long currentSpectatorCount = gameSpectatorRepository.countConfirmedSpectatorsByGame(gameId);
        if (game.hasReachedMaxSpectators((int) currentSpectatorCount)) {
            throw new BusinessException("Game has reached maximum number of spectators (" + game.getMaxSpectators() + ")");
        }
        
        // Create subscription
        GameSpectator gameSpectator = new GameSpectator();
        gameSpectator.setGame(game);
        gameSpectator.setSpectator(spectator);
        gameSpectator.setStatus(GameSpectator.SpectatorStatus.CONFIRMED);
        
        GameSpectator saved = gameSpectatorRepository.save(gameSpectator);
        
        return convertToResponse(saved);
    }
    
    /**
     * Spectator leaves a game
     */
    public void leaveGame(Long gameId) {
        // Get current user context
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        // Validate that only spectators can leave
        if (currentUser.getUserType() != UserType.SPECTATOR) {
            throw new BusinessException("Only spectators can leave games as spectators");
        }
        
        Long spectatorId = currentUser.getUserId();
        
        // Find subscription
        GameSpectator gameSpectator = gameSpectatorRepository.findByGameIdAndSpectatorId(gameId, spectatorId)
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
     * Get all games a spectator is subscribed to
     */
    public Page<GameSpectatorResponse> getMySubscribedGames(Pageable pageable) {
        // Get current user context
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        if (currentUser.getUserType() != UserType.SPECTATOR) {
            throw new BusinessException("Only spectators can view their subscribed games");
        }
        
        Long spectatorId = currentUser.getUserId();
        
        Page<GameSpectator> subscriptions = gameSpectatorRepository.findBySpectatorId(spectatorId, pageable);
        return subscriptions.map(this::convertToResponse);
    }
    
    /**
     * Check if current spectator is subscribed to a game
     */
    public boolean isSubscribed(Long gameId) {
        try {
            UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
            
            if (currentUser.getUserType() != UserType.SPECTATOR) {
                return false;
            }
            
            return gameSpectatorRepository.existsByGameIdAndSpectatorId(gameId, currentUser.getUserId());
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
    
    // Conversion method
    private GameSpectatorResponse convertToResponse(GameSpectator gameSpectator) {
        GameSpectatorResponse response = new GameSpectatorResponse();
        response.setId(gameSpectator.getId());
        response.setGameId(gameSpectator.getGame().getId());
        response.setGameName(gameSpectator.getGame().getGameName());
        response.setSpectatorId(gameSpectator.getSpectator().getId());
        response.setSpectatorUsername(gameSpectator.getSpectator().getRealUsername());
        response.setSpectatorName(gameSpectator.getSpectator().getName());
        response.setStatus(gameSpectator.getStatus());
        response.setJoinedAt(gameSpectator.getJoinedAt());
        response.setCreatedAt(gameSpectator.getCreatedAt());
        return response;
    }
}
