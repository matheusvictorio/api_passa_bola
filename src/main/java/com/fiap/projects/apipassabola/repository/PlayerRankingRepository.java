package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.Division;
import com.fiap.projects.apipassabola.entity.Player;
import com.fiap.projects.apipassabola.entity.PlayerRanking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRankingRepository extends JpaRepository<PlayerRanking, Long> {
    
    /**
     * Busca ranking de uma jogadora específica
     */
    Optional<PlayerRanking> findByPlayer(Player player);
    
    Optional<PlayerRanking> findByPlayerId(Long playerId);
    
    /**
     * Verifica se uma jogadora já tem ranking
     */
    boolean existsByPlayerId(Long playerId);
    
    /**
     * Busca rankings por divisão
     */
    Page<PlayerRanking> findByDivisionOrderByTotalPointsDesc(Division division, Pageable pageable);
    
    /**
     * Busca ranking global ordenado por pontos
     */
    Page<PlayerRanking> findAllByOrderByTotalPointsDesc(Pageable pageable);
    
    /**
     * Busca top N jogadoras
     */
    @Query("SELECT pr FROM PlayerRanking pr ORDER BY pr.totalPoints DESC, pr.gamesWon DESC")
    Page<PlayerRanking> findTopPlayers(Pageable pageable);
    
    /**
     * Busca posição de uma jogadora no ranking global
     */
    @Query("SELECT COUNT(pr) + 1 FROM PlayerRanking pr WHERE pr.totalPoints > :points OR (pr.totalPoints = :points AND pr.id < :playerId)")
    Long findPlayerPosition(@Param("points") Integer points, @Param("playerId") Long playerId);
    
    /**
     * Busca posição de uma jogadora dentro de sua divisão
     */
    @Query("SELECT COUNT(pr) + 1 FROM PlayerRanking pr WHERE pr.division = :division AND (pr.totalPoints > :points OR (pr.totalPoints = :points AND pr.id < :playerId))")
    Long findPlayerPositionInDivision(@Param("division") Division division, @Param("points") Integer points, @Param("playerId") Long playerId);
    
    /**
     * Busca jogadoras com melhor sequência de vitórias
     */
    @Query("SELECT pr FROM PlayerRanking pr WHERE pr.currentStreak > 0 ORDER BY pr.currentStreak DESC")
    Page<PlayerRanking> findPlayersWithWinStreak(Pageable pageable);
    
    /**
     * Busca jogadoras com maior taxa de vitória (mínimo de jogos)
     */
    @Query("SELECT pr FROM PlayerRanking pr WHERE pr.totalGames >= :minGames ORDER BY pr.winRate DESC, pr.totalPoints DESC")
    Page<PlayerRanking> findPlayersByWinRate(@Param("minGames") Integer minGames, Pageable pageable);
    
    /**
     * Conta total de jogadoras em uma divisão
     */
    Long countByDivision(Division division);
}
