package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.request.SpectatorRequest;
import com.fiap.projects.apipassabola.dto.response.OrganizationSummaryResponse;
import com.fiap.projects.apipassabola.dto.response.SpectatorResponse;
import com.fiap.projects.apipassabola.entity.Organization;
import com.fiap.projects.apipassabola.entity.Spectator;
import com.fiap.projects.apipassabola.exception.ResourceNotFoundException;
import com.fiap.projects.apipassabola.repository.OrganizationRepository;
import com.fiap.projects.apipassabola.repository.SpectatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SpectatorService {
    
    private final SpectatorRepository spectatorRepository;
    private final OrganizationRepository organizationRepository;
    
    public Page<SpectatorResponse> findAll(Pageable pageable) {
        return spectatorRepository.findAll(pageable).map(this::convertToResponse);
    }
    
    public SpectatorResponse findById(Long id) {
        Spectator spectator = spectatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Spectator", "id", id));
        return convertToResponse(spectator);
    }
    
    public SpectatorResponse findByUsername(String username) {
        Spectator spectator = spectatorRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Spectator", "username", username));
        return convertToResponse(spectator);
    }
    
    public Page<SpectatorResponse> findByName(String name, Pageable pageable) {
        return spectatorRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<SpectatorResponse> findByFavoriteTeam(Long favoriteTeamId, Pageable pageable) {
        return spectatorRepository.findByFavoriteTeamId(favoriteTeamId, pageable)
                .map(this::convertToResponse);
    }
    
    public SpectatorResponse update(Long id, SpectatorRequest request) {
        Spectator spectator = spectatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Spectator", "id", id));
        
        spectator.setFirstName(request.getFirstName());
        spectator.setLastName(request.getLastName());
        spectator.setBio(request.getBio());
        spectator.setBirthDate(request.getBirthDate());
        spectator.setProfilePhotoUrl(request.getProfilePhotoUrl());
        
        if (request.getFavoriteTeamId() != null) {
            Organization favoriteTeam = organizationRepository.findById(request.getFavoriteTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getFavoriteTeamId()));
            spectator.setFavoriteTeam(favoriteTeam);
        } else {
            spectator.setFavoriteTeam(null);
        }
        
        Spectator savedSpectator = spectatorRepository.save(spectator);
        return convertToResponse(savedSpectator);
    }
    
    public void delete(Long id) {
        if (!spectatorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Spectator", "id", id);
        }
        spectatorRepository.deleteById(id);
    }
    
    private SpectatorResponse convertToResponse(Spectator spectator) {
        SpectatorResponse response = new SpectatorResponse();
        response.setId(spectator.getId());
        response.setUserId(spectator.getUser().getId());
        response.setUsername(spectator.getUser().getUsername());
        response.setEmail(spectator.getUser().getEmail());
        response.setFirstName(spectator.getFirstName());
        response.setLastName(spectator.getLastName());
        response.setFullName(spectator.getFullName());
        response.setBio(spectator.getBio());
        response.setBirthDate(spectator.getBirthDate());
        response.setProfilePhotoUrl(spectator.getProfilePhotoUrl());
        response.setFollowedPlayersCount(spectatorRepository.countFollowedPlayersBySpectatorId(spectator.getId()).intValue());
        response.setFollowedOrganizationsCount(spectatorRepository.countFollowedOrganizationsBySpectatorId(spectator.getId()).intValue());
        response.setCreatedAt(spectator.getCreatedAt());
        response.setUpdatedAt(spectator.getUpdatedAt());
        
        if (spectator.getFavoriteTeam() != null) {
            response.setFavoriteTeamId(spectator.getFavoriteTeam().getId());
            OrganizationSummaryResponse favoriteTeamResponse = new OrganizationSummaryResponse();
            favoriteTeamResponse.setId(spectator.getFavoriteTeam().getId());
            favoriteTeamResponse.setName(spectator.getFavoriteTeam().getName());
            favoriteTeamResponse.setLogoUrl(spectator.getFavoriteTeam().getLogoUrl());
            favoriteTeamResponse.setCity(spectator.getFavoriteTeam().getCity());
            favoriteTeamResponse.setState(spectator.getFavoriteTeam().getState());
            response.setFavoriteTeam(favoriteTeamResponse);
        }
        
        return response;
    }
}
