package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    
    boolean existsByUserId(Long userId);
    
    Optional<Organization> findByUserId(Long userId);
    
    Optional<Organization> findByUsername(String username);
    
    Optional<Organization> findByEmail(String email);
    
    Optional<Organization> findByCnpj(String cnpj);
    
    @Query("SELECT o FROM Organization o WHERE LOWER(o.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Organization> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    
    @Query("SELECT COUNT(p) FROM Player p WHERE p.organization.id = :organizationId")
    Long countPlayersByOrganizationId(@Param("organizationId") Long organizationId);
    
    // Cross-type following queries for Organization
    @Query("SELECT p FROM Organization o JOIN o.followingPlayers p WHERE o.id = :organizationId")
    Page<com.fiap.projects.apipassabola.entity.Player> findFollowingPlayersByOrganizationId(@Param("organizationId") Long organizationId, Pageable pageable);
    
    @Query("SELECT s FROM Organization o JOIN o.followingSpectators s WHERE o.id = :organizationId")
    Page<com.fiap.projects.apipassabola.entity.Spectator> findFollowingSpectatorsByOrganizationId(@Param("organizationId") Long organizationId, Pageable pageable);
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Organization o JOIN o.followingPlayers p WHERE o.id = :organizationId AND p.id = :playerId")
    boolean isFollowingPlayer(@Param("organizationId") Long organizationId, @Param("playerId") Long playerId);
    
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Organization o JOIN o.followingSpectators s WHERE o.id = :organizationId AND s.id = :spectatorId")
    boolean isFollowingSpectator(@Param("organizationId") Long organizationId, @Param("spectatorId") Long spectatorId);
    
    // Cross-type followers queries for Organization
    @Query("SELECT p FROM Organization o JOIN o.playerFollowers p WHERE o.id = :organizationId")
    Page<com.fiap.projects.apipassabola.entity.Player> findPlayerFollowersByOrganizationId(@Param("organizationId") Long organizationId, Pageable pageable);
    
    @Query("SELECT s FROM Organization o JOIN o.spectatorFollowers s WHERE o.id = :organizationId")
    Page<com.fiap.projects.apipassabola.entity.Spectator> findSpectatorFollowersByOrganizationId(@Param("organizationId") Long organizationId, Pageable pageable);
    
    // Organization-to-Organization following queries (existing)
    @Query("SELECT o FROM Organization org JOIN org.followers o WHERE org.id = :organizationId")
    Page<Organization> findFollowersByOrganizationId(@Param("organizationId") Long organizationId, Pageable pageable);
    
    // Alias method for consistency - Organization followers from other Organizations
    @Query("SELECT o FROM Organization org JOIN org.followers o WHERE org.id = :organizationId")
    Page<Organization> findOrganizationFollowersByOrganizationId(@Param("organizationId") Long organizationId, Pageable pageable);
    
    @Query("SELECT o FROM Organization org JOIN org.following o WHERE org.id = :organizationId")
    Page<Organization> findFollowingByOrganizationId(@Param("organizationId") Long organizationId, Pageable pageable);
    
    // Alias method for consistency with the service layer
    @Query("SELECT o FROM Organization org JOIN org.following o WHERE org.id = :organizationId")
    Page<Organization> findFollowingOrganizationsByOrganizationId(@Param("organizationId") Long organizationId, Pageable pageable);
    
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Organization org JOIN org.following o WHERE org.id = :followerId AND o.id = :followedId")
    boolean isFollowingOrganization(@Param("followerId") Long followerId, @Param("followedId") Long followedId);
    
}
