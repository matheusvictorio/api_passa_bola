package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.TournamentTeam;
import com.fiap.projects.apipassabola.entity.Tournament;
import com.fiap.projects.apipassabola.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentTeamRepository extends JpaRepository<TournamentTeam, Long> {
    
    List<TournamentTeam> findByTournament(Tournament tournament);
    
    List<TournamentTeam> findByTournamentId(Long tournamentId);
    
    List<TournamentTeam> findByTeam(Team team);
    
    List<TournamentTeam> findByTeamId(Long teamId);
    
    Optional<TournamentTeam> findByTournamentIdAndTeamId(Long tournamentId, Long teamId);
    
    boolean existsByTournamentIdAndTeamId(Long tournamentId, Long teamId);
    
    @Query("SELECT COUNT(tt) FROM TournamentTeam tt WHERE tt.tournament.id = :tournamentId")
    long countByTournamentId(@Param("tournamentId") Long tournamentId);
    
    @Query("SELECT tt FROM TournamentTeam tt WHERE tt.tournament.id = :tournamentId ORDER BY tt.seedPosition ASC")
    List<TournamentTeam> findByTournamentIdOrderBySeedPosition(@Param("tournamentId") Long tournamentId);
    
    @Query("SELECT tt FROM TournamentTeam tt WHERE tt.tournament.id = :tournamentId AND tt.status = :status")
    List<TournamentTeam> findByTournamentIdAndStatus(
        @Param("tournamentId") Long tournamentId,
        @Param("status") TournamentTeam.TeamStatus status
    );
}
