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
    
    Optional<Spectator> findByUsername(String username);
    
    Optional<Spectator> findByEmail(String email);
    
    @Query("SELECT s FROM Spectator s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Spectator> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT s FROM Spectator s WHERE s.favoriteTeam.id = :teamId")
    Page<Spectator> findByFavoriteTeamId(@Param("teamId") Long teamId, Pageable pageable);
    
}
