package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.Team;
import com.fiap.projects.apipassabola.entity.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    
    // Find team by name
    Optional<Team> findByNameTeam(String nameTeam);
    
    // Find teams by leader
    List<Team> findByLeader(Player leader);
    
    // Find teams by leader id
    List<Team> findByLeaderId(Long leaderId);
    
    // Search teams by name containing (case insensitive)
    @Query("SELECT t FROM Team t WHERE LOWER(t.nameTeam) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Team> findByNameTeamContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    // Find teams where a specific player is a member
    @Query("SELECT t FROM Team t JOIN t.players p WHERE p.id = :playerId")
    List<Team> findTeamsByPlayerId(@Param("playerId") Long playerId);
    
    // Count teams by leader
    @Query("SELECT COUNT(t) FROM Team t WHERE t.leader.id = :leaderId")
    long countByLeaderId(@Param("leaderId") Long leaderId);
    
    // Check if team name already exists
    boolean existsByNameTeam(String nameTeam);
    
    // Find all teams with pagination
    Page<Team> findAll(Pageable pageable);
}
