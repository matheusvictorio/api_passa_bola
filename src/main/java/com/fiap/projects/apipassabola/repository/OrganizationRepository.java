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
    
    Optional<Organization> findByUserId(Long userId);
    
    Optional<Organization> findByUserUsername(String username);
    
    Optional<Organization> findByCnpj(String cnpj);
    
    @Query("SELECT o FROM Organization o WHERE LOWER(o.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Organization> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT o FROM Organization o WHERE LOWER(o.city) = LOWER(:city)")
    Page<Organization> findByCityIgnoreCase(@Param("city") String city, Pageable pageable);
    
    @Query("SELECT o FROM Organization o WHERE LOWER(o.state) = LOWER(:state)")
    Page<Organization> findByStateIgnoreCase(@Param("state") String state, Pageable pageable);
    
    @Query("SELECT o FROM Organization o WHERE o.foundedYear = :year")
    Page<Organization> findByFoundedYear(@Param("year") Integer year, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Organization o JOIN o.players p WHERE o.id = :organizationId")
    Long countPlayersByOrganizationId(@Param("organizationId") Long organizationId);
    
    @Query("SELECT DISTINCT o.city FROM Organization o ORDER BY o.city")
    Page<String> findDistinctCities(Pageable pageable);
    
    @Query("SELECT DISTINCT o.state FROM Organization o ORDER BY o.state")
    Page<String> findDistinctStates(Pageable pageable);
}
