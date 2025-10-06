package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.Organization;
import com.fiap.projects.apipassabola.entity.Player;
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
    
    boolean existsByUserId(Long userId);
    
    Optional<Spectator> findByUserId(Long userId);
    
    Optional<Spectator> findByUsername(String username);
    
    Optional<Spectator> findByEmail(String email);
    
    @Query("SELECT s FROM Spectator s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Spectator> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT s FROM Spectator s WHERE s.favoriteTeam.id = :teamId")
    Page<Spectator> findByFavoriteTeamId(@Param("teamId") Long teamId, Pageable pageable);
    
    // Following/Followers queries - Spectator to Spectator
    @Query("SELECT s.followers FROM Spectator s WHERE s.id = :spectatorId")
    Page<Spectator> findFollowersBySpectatorId(@Param("spectatorId") Long spectatorId, Pageable pageable);
    
    @Query("SELECT s.following FROM Spectator s WHERE s.id = :spectatorId")
    Page<Spectator> findFollowingBySpectatorId(@Param("spectatorId") Long spectatorId, Pageable pageable);
    
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Spectator s JOIN s.following f WHERE s.id = :followerId AND f.id = :followedId")
    boolean isFollowing(@Param("followerId") Long followerId, @Param("followedId") Long followedId);
    
    // Cross-type following queries - Spectator following Players
    @Query("SELECT s.followingPlayers FROM Spectator s WHERE s.id = :spectatorId")
    Page<Player> findFollowingPlayersBySpectatorId(@Param("spectatorId") Long spectatorId, Pageable pageable);
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Spectator s JOIN s.followingPlayers p WHERE s.id = :spectatorId AND p.id = :playerId")
    boolean isFollowingPlayer(@Param("spectatorId") Long spectatorId, @Param("playerId") Long playerId);
    
    // Cross-type following queries - Spectator following Organizations
    @Query("SELECT s.followingOrganizations FROM Spectator s WHERE s.id = :spectatorId")
    Page<Organization> findFollowingOrganizationsBySpectatorId(@Param("spectatorId") Long spectatorId, Pageable pageable);
    
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Spectator s JOIN s.followingOrganizations o WHERE s.id = :spectatorId AND o.id = :organizationId")
    boolean isFollowingOrganization(@Param("spectatorId") Long spectatorId, @Param("organizationId") Long organizationId);
    
    // Cross-type followers queries - Players following Spectator
    @Query("SELECT s.playerFollowers FROM Spectator s WHERE s.id = :spectatorId")
    Page<Player> findPlayerFollowersBySpectatorId(@Param("spectatorId") Long spectatorId, Pageable pageable);
    
    // Cross-type followers queries - Organizations following Spectator
    @Query("SELECT s.organizationFollowers FROM Spectator s WHERE s.id = :spectatorId")
    Page<Organization> findOrganizationFollowersBySpectatorId(@Param("spectatorId") Long spectatorId, Pageable pageable);
    
}
