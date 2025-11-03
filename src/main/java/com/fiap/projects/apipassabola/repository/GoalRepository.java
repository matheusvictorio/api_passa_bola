package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    
    List<Goal> findByGameId(Long gameId);
    
    List<Goal> findByPlayerId(Long playerId);
    
    List<Goal> findByGameIdAndTeamSide(Long gameId, Integer teamSide);
    
    List<Goal> findByGameIdAndIsOwnGoal(Long gameId, Boolean isOwnGoal);
    
    @Query("SELECT g FROM Goal g WHERE g.game.id = :gameId AND g.isOwnGoal = false ORDER BY g.teamSide, g.minute")
    List<Goal> findGoalsByGame(Long gameId);
    
    @Query("SELECT COUNT(g) FROM Goal g WHERE g.player.id = :playerId AND g.isOwnGoal = false")
    Long countGoalsByPlayer(Long playerId);
    
    @Query("SELECT g FROM Goal g WHERE g.player.id = :playerId AND g.isOwnGoal = false ORDER BY g.createdAt DESC")
    List<Goal> findGoalsByPlayerOrderByDate(Long playerId);
}
