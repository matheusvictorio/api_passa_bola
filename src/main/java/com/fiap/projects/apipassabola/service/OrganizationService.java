package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.request.OrganizationRequest;
import com.fiap.projects.apipassabola.dto.response.OrganizationResponse;
import com.fiap.projects.apipassabola.dto.response.OrganizationSummaryResponse;
import com.fiap.projects.apipassabola.entity.Organization;
import com.fiap.projects.apipassabola.exception.BusinessException;
import com.fiap.projects.apipassabola.exception.ResourceNotFoundException;
import com.fiap.projects.apipassabola.repository.GameRepository;
import com.fiap.projects.apipassabola.repository.OrganizationRepository;
import com.fiap.projects.apipassabola.repository.PlayerRepository;
import com.fiap.projects.apipassabola.util.CnpjValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationService {
    
    private final OrganizationRepository organizationRepository;
    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    
    public Page<OrganizationResponse> findAll(Pageable pageable) {
        return organizationRepository.findAll(pageable).map(this::convertToResponse);
    }
    
    public OrganizationResponse findById(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        return convertToResponse(organization);
    }
    
    public OrganizationResponse findByUsername(String username) {
        Organization organization = organizationRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "username", username));
        return convertToResponse(organization);
    }
    
    public Page<OrganizationResponse> findByName(String name, Pageable pageable) {
        return organizationRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<OrganizationResponse> findByCity(String city, Pageable pageable) {
        return organizationRepository.findByCityIgnoreCase(city, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<OrganizationResponse> findByState(String state, Pageable pageable) {
        return organizationRepository.findByStateIgnoreCase(state, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<OrganizationResponse> findByFoundedYear(Integer foundedYear, Pageable pageable) {
        return organizationRepository.findByFoundedYear(foundedYear, pageable)
                .map(this::convertToResponse);
    }
    
    public OrganizationResponse update(Long id, OrganizationRequest request) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        
        // Validate CNPJ uniqueness if it's being changed
        String normalizedCnpj = CnpjValidator.unformat(request.getCnpj());
        if (!normalizedCnpj.equals(organization.getCnpj())) {
            Optional<Organization> existingOrg = organizationRepository.findByCnpj(normalizedCnpj);
            if (existingOrg.isPresent()) {
                throw new BusinessException("CNPJ already exists for another organization");
            }
        }
        
        organization.setName(request.getName());
        organization.setDescription(request.getDescription());
        organization.setCity(request.getCity());
        organization.setState(request.getState());
        organization.setLogoUrl(request.getLogoUrl());
        organization.setPrimaryColors(request.getPrimaryColors());
        organization.setFoundedYear(request.getFoundedYear());
        organization.setWebsiteUrl(request.getWebsiteUrl());
        organization.setContactEmail(request.getContactEmail());
        organization.setContactPhone(request.getContactPhone());
        organization.setCnpj(normalizedCnpj);
        
        Organization savedOrganization = organizationRepository.save(organization);
        return convertToResponse(savedOrganization);
    }
    
    public void delete(Long id) {
        if (!organizationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Organization", "id", id);
        }
        
        // Check if organization has players
        long playerCount = playerRepository.countByOrganizationId(id);
        if (playerCount > 0) {
            throw new BusinessException("Cannot delete organization with " + playerCount + " players. Remove players first.");
        }
        
        organizationRepository.deleteById(id);
    }
    
    private OrganizationResponse convertToResponse(Organization organization) {
        OrganizationResponse response = new OrganizationResponse();
        response.setId(organization.getId());
        response.setUserId(organization.getUser().getId());
        response.setUsername(organization.getUser().getUsername());
        response.setEmail(organization.getUser().getEmail());
        response.setName(organization.getName());
        response.setDescription(organization.getDescription());
        response.setCity(organization.getCity());
        response.setState(organization.getState());
        response.setLogoUrl(organization.getLogoUrl());
        response.setPrimaryColors(organization.getPrimaryColors());
        response.setFoundedYear(organization.getFoundedYear());
        response.setWebsiteUrl(organization.getWebsiteUrl());
        response.setContactEmail(organization.getContactEmail());
        response.setContactPhone(organization.getContactPhone());
        response.setCnpj(CnpjValidator.format(organization.getCnpj()));
        response.setPlayersCount(playerRepository.countByOrganizationId(organization.getId()).intValue());
        response.setTotalGames(gameRepository.countByHomeTeamIdOrAwayTeamId(organization.getId(), organization.getId()).intValue());
        response.setCreatedAt(organization.getCreatedAt());
        response.setUpdatedAt(organization.getUpdatedAt());
        
        return response;
    }
    
    public OrganizationSummaryResponse convertToSummaryResponse(Organization organization) {
        OrganizationSummaryResponse response = new OrganizationSummaryResponse();
        response.setId(organization.getId());
        response.setName(organization.getName());
        response.setLogoUrl(organization.getLogoUrl());
        response.setCity(organization.getCity());
        response.setState(organization.getState());
        
        return response;
    }
}
