package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.Spectator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpectatorRepository extends JpaRepository<Spectator, Long> {
    
    Optional<Spectator> findByUserId(Long userId);
    
    Optional<Spectator> findByUserUsername(String username);
    
    @Query("SELECT s FROM Spectator s WHERE LOWER(s.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Spectator> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT s FROM Spectator s WHERE s.favoriteTeam.id = :teamId")
    Page<Spectator> findByFavoriteTeamId(@Param("teamId") Long teamId, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Spectator s JOIN s.followedPlayers p WHERE s.id = :spectatorId")
    Long countFollowedPlayersBySpectatorId(@Param("spectatorId") Long spectatorId);
    
    @Query("SELECT COUNT(o) FROM Spectator s JOIN s.followedOrganizations o WHERE s.id = :spectatorId")
    Long countFollowedOrganizationsBySpectatorId(@Param("spectatorId") Long spectatorId);
}
