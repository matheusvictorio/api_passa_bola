package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.TournamentMatch;
import com.fiap.projects.apipassabola.entity.Tournament;
import com.fiap.projects.apipassabola.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentMatchRepository extends JpaRepository<TournamentMatch, Long> {
    
    List<TournamentMatch> findByTournament(Tournament tournament);
    
    List<TournamentMatch> findByTournamentId(Long tournamentId);
    
    @Query("SELECT tm FROM TournamentMatch tm WHERE tm.tournament.id = :tournamentId AND tm.round = :round ORDER BY tm.matchNumber ASC")
    List<TournamentMatch> findByTournamentIdAndRound(
        @Param("tournamentId") Long tournamentId,
        @Param("round") String round
    );
    
    @Query("SELECT tm FROM TournamentMatch tm WHERE tm.tournament.id = :tournamentId AND tm.status = :status")
    List<TournamentMatch> findByTournamentIdAndStatus(
        @Param("tournamentId") Long tournamentId,
        @Param("status") TournamentMatch.MatchStatus status
    );
    
    @Query("SELECT tm FROM TournamentMatch tm WHERE tm.team1.id = :teamId OR tm.team2.id = :teamId")
    List<TournamentMatch> findByTeamId(@Param("teamId") Long teamId);
    
    @Query("SELECT tm FROM TournamentMatch tm WHERE tm.tournament.id = :tournamentId AND (tm.team1.id = :teamId OR tm.team2.id = :teamId)")
    List<TournamentMatch> findByTournamentIdAndTeamId(
        @Param("tournamentId") Long tournamentId,
        @Param("teamId") Long teamId
    );
    
    @Query("SELECT tm FROM TournamentMatch tm WHERE tm.tournament.id = :tournamentId ORDER BY tm.round DESC, tm.matchNumber ASC")
    List<TournamentMatch> findByTournamentIdOrderByRound(@Param("tournamentId") Long tournamentId);
    
    Optional<TournamentMatch> findByTournamentIdAndRoundAndMatchNumber(
        Long tournamentId, 
        String round, 
        Integer matchNumber
    );
    
    @Query("SELECT COUNT(tm) FROM TournamentMatch tm WHERE tm.tournament.id = :tournamentId AND tm.round = :round")
    long countByTournamentIdAndRound(
        @Param("tournamentId") Long tournamentId,
        @Param("round") String round
    );
}
