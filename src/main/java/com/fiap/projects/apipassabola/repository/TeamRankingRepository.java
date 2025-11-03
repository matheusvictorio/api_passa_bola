package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.Division;
import com.fiap.projects.apipassabola.entity.Team;
import com.fiap.projects.apipassabola.entity.TeamRanking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRankingRepository extends JpaRepository<TeamRanking, Long> {
    
    /**
     * Busca ranking de um time específico
     */
    Optional<TeamRanking> findByTeam(Team team);
    
    Optional<TeamRanking> findByTeamId(Long teamId);
    
    /**
     * Verifica se um time já tem ranking
     */
    boolean existsByTeamId(Long teamId);
    
    /**
     * Busca rankings por divisão
     */
    Page<TeamRanking> findByDivisionOrderByTotalPointsDesc(Division division, Pageable pageable);
    
    /**
     * Busca ranking global ordenado por pontos
     */
    Page<TeamRanking> findAllByOrderByTotalPointsDesc(Pageable pageable);
    
    /**
     * Busca top N times
     */
    @Query("SELECT tr FROM TeamRanking tr ORDER BY tr.totalPoints DESC, tr.gamesWon DESC")
    Page<TeamRanking> findTopTeams(Pageable pageable);
    
    /**
     * Busca posição de um time no ranking global
     */
    @Query("SELECT COUNT(tr) + 1 FROM TeamRanking tr WHERE tr.totalPoints > :points OR (tr.totalPoints = :points AND tr.id < :teamId)")
    Long findTeamPosition(@Param("points") Integer points, @Param("teamId") Long teamId);
    
    /**
     * Busca posição de um time dentro de sua divisão
     */
    @Query("SELECT COUNT(tr) + 1 FROM TeamRanking tr WHERE tr.division = :division AND (tr.totalPoints > :points OR (tr.totalPoints = :points AND tr.id < :teamId))")
    Long findTeamPositionInDivision(@Param("division") Division division, @Param("points") Integer points, @Param("teamId") Long teamId);
    
    /**
     * Busca times com melhor sequência de vitórias
     */
    @Query("SELECT tr FROM TeamRanking tr WHERE tr.currentStreak > 0 ORDER BY tr.currentStreak DESC")
    Page<TeamRanking> findTeamsWithWinStreak(Pageable pageable);
    
    /**
     * Busca times com maior taxa de vitória (mínimo de jogos)
     */
    @Query("SELECT tr FROM TeamRanking tr WHERE tr.totalGames >= :minGames ORDER BY tr.winRate DESC, tr.totalPoints DESC")
    Page<TeamRanking> findTeamsByWinRate(@Param("minGames") Integer minGames, Pageable pageable);
    
    /**
     * Conta total de times em uma divisão
     */
    Long countByDivision(Division division);
}
