package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.GameParticipant;
import com.fiap.projects.apipassabola.entity.Game;
import com.fiap.projects.apipassabola.entity.Player;
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
public interface GameParticipantRepository extends JpaRepository<GameParticipant, Long> {
    
    // Find participants by game
    List<GameParticipant> findByGame(Game game);
    Page<GameParticipant> findByGame(Game game, Pageable pageable);
    
    // Find participants by game ID
    List<GameParticipant> findByGameId(Long gameId);
    Page<GameParticipant> findByGameId(Long gameId, Pageable pageable);
    
    // Find participants by player
    List<GameParticipant> findByPlayer(Player player);
    Page<GameParticipant> findByPlayer(Player player, Pageable pageable);
    
    // Find participants by player ID
    List<GameParticipant> findByPlayerId(Long playerId);
    Page<GameParticipant> findByPlayerId(Long playerId, Pageable pageable);
    
    // Find participants by multiple player IDs
    Page<GameParticipant> findByPlayerIdIn(List<Long> playerIds, Pageable pageable);
    
    // Find participants by game and team side
    List<GameParticipant> findByGameIdAndTeamSide(Long gameId, Integer teamSide);
    
    // Find participants by game and participation type
    List<GameParticipant> findByGameIdAndParticipationType(Long gameId, GameParticipant.ParticipationType participationType);
    
    // Find participants by game and status
    List<GameParticipant> findByGameIdAndStatus(Long gameId, GameParticipant.ParticipationStatus status);
    
    // Check if player is already participating in a game
    boolean existsByGameIdAndPlayerId(Long gameId, Long playerId);
    
    // Find specific participation
    Optional<GameParticipant> findByGameIdAndPlayerId(Long gameId, Long playerId);
    
    // Count participants by game
    long countByGameId(Long gameId);
    
    // Count participants by game and team side
    long countByGameIdAndTeamSide(Long gameId, Integer teamSide);
    
    // Count participants by game and status
    long countByGameIdAndStatus(Long gameId, GameParticipant.ParticipationStatus status);
    
    // Find participants by team side
    @Query("SELECT gp FROM GameParticipant gp WHERE gp.game.id = :gameId AND gp.teamSide = :teamSide AND gp.status = 'CONFIRMED'")
    List<GameParticipant> findConfirmedParticipantsByGameAndTeamSide(@Param("gameId") Long gameId, @Param("teamSide") Integer teamSide);
    
    // Find all confirmed participants for a game
    @Query("SELECT gp FROM GameParticipant gp WHERE gp.game.id = :gameId AND gp.status = 'CONFIRMED'")
    List<GameParticipant> findConfirmedParticipantsByGame(@Param("gameId") Long gameId);
    
    // Find participants by player's team (when joining with team)
    @Query("SELECT gp FROM GameParticipant gp JOIN gp.player.teams t WHERE gp.game.id = :gameId AND t.id = :teamId AND gp.participationType = 'WITH_TEAM'")
    List<GameParticipant> findByGameIdAndPlayerTeamId(@Param("gameId") Long gameId, @Param("teamId") Long teamId);
}
