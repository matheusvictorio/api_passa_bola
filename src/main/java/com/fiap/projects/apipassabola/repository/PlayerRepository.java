package com.fiap.projects.apipassabola.repository;

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
public interface PlayerRepository extends JpaRepository<Player, Long> {
    
    Optional<Player> findByUserId(Long userId);
    
    Optional<Player> findByUserUsername(String username);
    
    @Query("SELECT p FROM Player p WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Player> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT p FROM Player p WHERE p.organization.id = :organizationId")
    Page<Player> findByOrganizationId(@Param("organizationId") Long organizationId, Pageable pageable);
    
    @Query("SELECT p FROM Player p WHERE p.position = :position")
    Page<Player> findByPosition(@Param("position") Player.Position position, Pageable pageable);
    
    @Query("SELECT p FROM Player p WHERE p.jerseyNumber = :number AND p.organization.id = :organizationId")
    Optional<Player> findByJerseyNumberAndOrganizationId(@Param("number") Integer number, @Param("organizationId") Long organizationId);
    
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
}
