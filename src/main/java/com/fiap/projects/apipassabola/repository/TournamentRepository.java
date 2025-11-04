package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.Tournament;
import com.fiap.projects.apipassabola.entity.GameType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    
    List<Tournament> findByGameType(GameType gameType);
    
    Page<Tournament> findByGameType(GameType gameType, Pageable pageable);
    
    List<Tournament> findByCreatorId(Long creatorId);
    
    Page<Tournament> findByCreatorId(Long creatorId, Pageable pageable);
    
    List<Tournament> findByStatus(Tournament.TournamentStatus status);
    
    Page<Tournament> findByStatus(Tournament.TournamentStatus status, Pageable pageable);
    
    @Query("SELECT t FROM Tournament t WHERE t.status = :status AND t.gameType = :gameType")
    List<Tournament> findByStatusAndGameType(
        @Param("status") Tournament.TournamentStatus status,
        @Param("gameType") GameType gameType
    );
    
    @Query("SELECT t FROM Tournament t WHERE t.name LIKE %:name%")
    List<Tournament> searchByName(@Param("name") String name);
    
    @Query("SELECT t FROM Tournament t WHERE t.name LIKE %:name%")
    Page<Tournament> searchByName(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT t FROM Tournament t WHERE t.bracketGenerated = true AND t.status = 'IN_PROGRESS'")
    List<Tournament> findActiveTournaments();
    
    @Query("SELECT t FROM Tournament t WHERE t.status = 'REGISTRATION' AND t.totalTeams < t.maxTeams")
    List<Tournament> findOpenForRegistration();
}
