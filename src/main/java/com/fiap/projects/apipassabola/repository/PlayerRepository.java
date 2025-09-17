package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.Player;
import com.fiap.projects.apipassabola.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    
    Optional<Player> findByUsername(String username);
    
    Optional<Player> findByEmail(String email);
    
    @Query("SELECT p FROM Player p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Player> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT p FROM Player p WHERE p.organization.id = :organizationId")
    Page<Player> findByOrganizationId(@Param("organizationId") Long organizationId, Pageable pageable);
    
    @Query("SELECT p FROM Player p WHERE p.organization.id = :organizationId")
    List<Player> findByOrganizationId(@Param("organizationId") Long organizationId);
    
    
    @Query("SELECT COUNT(f) FROM Player p JOIN p.followers f WHERE p.id = :playerId")
    Long countFollowersByPlayerId(@Param("playerId") Long playerId);
    
    @Query("SELECT COUNT(f) FROM Player p JOIN p.following f WHERE p.id = :playerId")
    Long countFollowingByPlayerId(@Param("playerId") Long playerId);
    
    @Query("SELECT p FROM Player p JOIN p.followers f WHERE f.id = :followerId")
    Page<Player> findFollowedPlayersByFollowerId(@Param("followerId") Long followerId, Pageable pageable);
    
    @Query("SELECT f FROM Player p JOIN p.followers f WHERE p.id = :playerId")
    Page<Player> findFollowersByPlayerId(@Param("playerId") Long playerId, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Player p WHERE p.organization.id = :organizationId")
    Long countByOrganizationId(@Param("organizationId") Long organizationId);
    
    // Team-related queries for ManyToMany relationship
    @Query("SELECT p FROM Player p JOIN p.teams t WHERE t.id = :teamId")
    List<Player> findByTeamId(@Param("teamId") Long teamId);
    
    @Query("SELECT COUNT(p) FROM Player p JOIN p.teams t WHERE t.id = :teamId")
    long countByTeamId(@Param("teamId") Long teamId);
    
    @Query("SELECT p FROM Player p JOIN p.teams t WHERE t = :team")
    List<Player> findByTeamsContaining(@Param("team") Team team);
    
    // Cross-type following queries for Player
    @Query("SELECT s FROM Player p JOIN p.followingSpectators s WHERE p.id = :playerId")
    Page<com.fiap.projects.apipassabola.entity.Spectator> findFollowingSpectatorsByPlayerId(@Param("playerId") Long playerId, Pageable pageable);
    
    @Query("SELECT o FROM Player p JOIN p.followingOrganizations o WHERE p.id = :playerId")
    Page<com.fiap.projects.apipassabola.entity.Organization> findFollowingOrganizationsByPlayerId(@Param("playerId") Long playerId, Pageable pageable);
    
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Player p JOIN p.followingSpectators s WHERE p.id = :playerId AND s.id = :spectatorId")
    boolean isFollowingSpectator(@Param("playerId") Long playerId, @Param("spectatorId") Long spectatorId);
    
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Player p JOIN p.followingOrganizations o WHERE p.id = :playerId AND o.id = :organizationId")
    boolean isFollowingOrganization(@Param("playerId") Long playerId, @Param("organizationId") Long organizationId);
    
    // Player-to-Player following query (missing method)
    @Query("SELECT f FROM Player p JOIN p.following f WHERE p.id = :playerId")
    Page<Player> findFollowingByPlayerId(@Param("playerId") Long playerId, Pageable pageable);
    
    // Cross-type followers queries for Player
    @Query("SELECT s FROM Player p JOIN p.spectatorFollowers s WHERE p.id = :playerId")
    Page<com.fiap.projects.apipassabola.entity.Spectator> findSpectatorFollowersByPlayerId(@Param("playerId") Long playerId, Pageable pageable);
    
    @Query("SELECT o FROM Player p JOIN p.organizationFollowers o WHERE p.id = :playerId")
    Page<com.fiap.projects.apipassabola.entity.Organization> findOrganizationFollowersByPlayerId(@Param("playerId") Long playerId, Pageable pageable);
}
