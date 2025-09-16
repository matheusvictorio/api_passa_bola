package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.Game;
import com.fiap.projects.apipassabola.entity.GameType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    
    @Query("SELECT g FROM Game g WHERE g.homeTeam.id = :organizationId OR g.awayTeam.id = :organizationId")
    Page<Game> findByOrganizationId(@Param("organizationId") Long organizationId, Pageable pageable);
    
    @Query("SELECT g FROM Game g WHERE g.status = :status")
    Page<Game> findByStatus(@Param("status") Game.GameStatus status, Pageable pageable);
    
    @Query("SELECT g FROM Game g WHERE LOWER(g.championship) LIKE LOWER(CONCAT('%', :championship, '%'))")
    Page<Game> findByChampionshipContainingIgnoreCase(@Param("championship") String championship, Pageable pageable);
    
    @Query("SELECT g FROM Game g WHERE g.gameDate >= :startDate AND g.gameDate <= :endDate")
    Page<Game> findByGameDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
    
    @Query("SELECT g FROM Game g WHERE g.gameDate > :currentDate AND g.status = 'SCHEDULED' ORDER BY g.gameDate ASC")
    Page<Game> findUpcomingGames(@Param("currentDate") LocalDateTime currentDate, Pageable pageable);
    
    @Query("SELECT g FROM Game g WHERE g.status = 'FINISHED' ORDER BY g.gameDate DESC")
    Page<Game> findFinishedGames(Pageable pageable);
    
    @Query("SELECT g FROM Game g WHERE g.status = 'LIVE'")
    Page<Game> findLiveGames(Pageable pageable);
    
    @Query("SELECT COUNT(g) FROM Game g WHERE (g.homeTeam.id = :organizationId OR g.awayTeam.id = :organizationId) AND g.status = 'FINISHED'")
    Long countFinishedGamesByOrganizationId(@Param("organizationId") Long organizationId);
    
    @Query("SELECT COUNT(g) FROM Game g WHERE g.homeTeam.id = :organizationId AND g.homeGoals > g.awayGoals AND g.status = 'FINISHED'")
    Long countHomeWinsByOrganizationId(@Param("organizationId") Long organizationId);
    
    @Query("SELECT COUNT(g) FROM Game g WHERE g.awayTeam.id = :organizationId AND g.awayGoals > g.homeGoals AND g.status = 'FINISHED'")
    Long countAwayWinsByOrganizationId(@Param("organizationId") Long organizationId);
    
    @Query("SELECT g FROM Game g WHERE g.homeTeam.id = :homeTeamId OR g.awayTeam.id = :awayTeamId")
    Page<Game> findByHomeTeamIdOrAwayTeamId(@Param("homeTeamId") Long homeTeamId, @Param("awayTeamId") Long awayTeamId, Pageable pageable);
    
    @Query("SELECT COUNT(g) FROM Game g WHERE g.homeTeam.id = :homeTeamId OR g.awayTeam.id = :awayTeamId")
    Long countByHomeTeamIdOrAwayTeamId(@Param("homeTeamId") Long homeTeamId, @Param("awayTeamId") Long awayTeamId);
    
    // Queries for new game types
    Page<Game> findByGameType(GameType gameType, Pageable pageable);
    List<Game> findByGameType(GameType gameType);
    
    // Queries for FRIENDLY and CHAMPIONSHIP games
    Page<Game> findByGameTypeAndHostId(GameType gameType, Long hostId, Pageable pageable);
    List<Game> findByGameTypeAndHostId(GameType gameType, Long hostId);
    
    @Query("SELECT g FROM Game g WHERE g.gameType IN ('FRIENDLY', 'CHAMPIONSHIP') AND LOWER(g.gameName) LIKE LOWER(CONCAT('%', :gameName, '%'))")
    Page<Game> findFriendlyAndChampionshipByGameNameContaining(@Param("gameName") String gameName, Pageable pageable);
    
    @Query("SELECT g FROM Game g WHERE g.gameType IN ('FRIENDLY', 'CHAMPIONSHIP') AND g.hostUsername = :hostUsername")
    Page<Game> findFriendlyAndChampionshipByHostUsername(@Param("hostUsername") String hostUsername, Pageable pageable);
    
    // Queries for CUP games
    @Query("SELECT g FROM Game g WHERE g.gameType = 'CUP' AND (g.homeTeam.id = :teamId OR g.awayTeam.id = :teamId)")
    Page<Game> findCupGamesByTeamId(@Param("teamId") Long teamId, Pageable pageable);
    
    @Query("SELECT g FROM Game g WHERE g.gameType = 'CUP' AND LOWER(g.championship) LIKE LOWER(CONCAT('%', :championship, '%'))")
    Page<Game> findCupGamesByChampionship(@Param("championship") String championship, Pageable pageable);
    
    // Count queries for statistics
    long countByGameType(GameType gameType);
    long countByGameTypeAndHostId(GameType gameType, Long hostId);
    
    @Query("SELECT COUNT(g) FROM Game g WHERE g.gameType = 'CUP' AND (g.homeTeam.id = :teamId OR g.awayTeam.id = :teamId)")
    long countCupGamesByTeamId(@Param("teamId") Long teamId);
}
