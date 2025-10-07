package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.GameSpectator;
import com.fiap.projects.apipassabola.entity.Game;
import com.fiap.projects.apipassabola.entity.Spectator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameSpectatorRepository extends JpaRepository<GameSpectator, Long> {
    
    // Find spectators by game
    List<GameSpectator> findByGame(Game game);
    Page<GameSpectator> findByGame(Game game, Pageable pageable);
    
    // Find spectators by game ID
    List<GameSpectator> findByGameId(Long gameId);
    Page<GameSpectator> findByGameId(Long gameId, Pageable pageable);
    
    // Find games by spectator
    List<GameSpectator> findBySpectator(Spectator spectator);
    Page<GameSpectator> findBySpectator(Spectator spectator, Pageable pageable);
    
    // Find games by spectator ID
    List<GameSpectator> findBySpectatorId(Long spectatorId);
    Page<GameSpectator> findBySpectatorId(Long spectatorId, Pageable pageable);
    
    // Find spectators by game and status
    List<GameSpectator> findByGameIdAndStatus(Long gameId, GameSpectator.SpectatorStatus status);
    
    // Check if spectator is already subscribed to a game
    boolean existsByGameIdAndSpectatorId(Long gameId, Long spectatorId);
    
    // Find specific subscription
    Optional<GameSpectator> findByGameIdAndSpectatorId(Long gameId, Long spectatorId);
    
    // Count spectators by game
    long countByGameId(Long gameId);
    
    // Count confirmed spectators by game
    long countByGameIdAndStatus(Long gameId, GameSpectator.SpectatorStatus status);
    
    // Find all confirmed spectators for a game
    @Query("SELECT gs FROM GameSpectator gs WHERE gs.game.id = :gameId AND gs.status = 'CONFIRMED'")
    List<GameSpectator> findConfirmedSpectatorsByGame(@Param("gameId") Long gameId);
    
    // Count confirmed spectators
    @Query("SELECT COUNT(gs) FROM GameSpectator gs WHERE gs.game.id = :gameId AND gs.status = 'CONFIRMED'")
    long countConfirmedSpectatorsByGame(@Param("gameId") Long gameId);
}
