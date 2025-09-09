package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

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
}
