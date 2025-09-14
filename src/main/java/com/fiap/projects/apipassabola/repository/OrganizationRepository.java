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
    
    Optional<Organization> findByUsername(String username);
    
    Optional<Organization> findByEmail(String email);
    
    Optional<Organization> findByCnpj(String cnpj);
    
    @Query("SELECT o FROM Organization o WHERE LOWER(o.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Organization> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    
    @Query("SELECT COUNT(p) FROM Player p WHERE p.organization.id = :organizationId")
    Long countPlayersByOrganizationId(@Param("organizationId") Long organizationId);
    
}
