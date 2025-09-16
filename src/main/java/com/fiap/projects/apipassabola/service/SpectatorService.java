package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.request.SpectatorRequest;
import com.fiap.projects.apipassabola.dto.response.OrganizationSummaryResponse;
import com.fiap.projects.apipassabola.dto.response.PlayerResponse;
import com.fiap.projects.apipassabola.dto.response.OrganizationResponse;
import com.fiap.projects.apipassabola.dto.response.SpectatorResponse;
import com.fiap.projects.apipassabola.entity.Organization;
import com.fiap.projects.apipassabola.entity.Player;
import com.fiap.projects.apipassabola.entity.Spectator;
import com.fiap.projects.apipassabola.exception.ResourceNotFoundException;
import com.fiap.projects.apipassabola.repository.OrganizationRepository;
import com.fiap.projects.apipassabola.repository.PlayerRepository;
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
    private final PlayerRepository playerRepository;
    private final UserContextService userContextService;
    
    public Page<SpectatorResponse> findAll(Pageable pageable) {
        return spectatorRepository.findAll(pageable).map(this::convertToResponse);
    }
    
    public SpectatorResponse findById(Long id) {
        Spectator spectator = spectatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Spectator", "id", id));
        return convertToResponse(spectator);
    }
    
    public SpectatorResponse findByUsername(String username) {
        Spectator spectator = spectatorRepository.findByUsername(username)
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
        
        spectator.setUsername(request.getUsername());
        spectator.setName(request.getName());
        spectator.setEmail(request.getEmail());
        spectator.setBio(request.getBio());
        spectator.setBirthDate(request.getBirthDate());
        spectator.setPhone(request.getPhone());
        spectator.setProfilePhotoUrl(request.getProfilePhotoUrl());
        spectator.setBannerUrl(request.getBannerUrl());
        
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
        response.setUserType(spectator.getUserType());
        response.setUsername(spectator.getRealUsername()); // Use getRealUsername() to get actual username, not email
        response.setName(spectator.getName());
        response.setEmail(spectator.getEmail());
        response.setBio(spectator.getBio());
        response.setBirthDate(spectator.getBirthDate());
        response.setPhone(spectator.getPhone());
        response.setProfilePhotoUrl(spectator.getProfilePhotoUrl());
        response.setBannerUrl(spectator.getBannerUrl());
        response.setFollowersCount(spectator.getFollowersCount());
        response.setFollowingCount(spectator.getFollowingCount());
        response.setCreatedAt(spectator.getCreatedAt());
        response.setUpdatedAt(spectator.getUpdatedAt());
        
        if (spectator.getFavoriteTeam() != null) {
            response.setFavoriteTeamId(spectator.getFavoriteTeam().getId());
            OrganizationSummaryResponse favoriteTeamResponse = new OrganizationSummaryResponse();
            favoriteTeamResponse.setId(spectator.getFavoriteTeam().getId());
            favoriteTeamResponse.setName(spectator.getFavoriteTeam().getName());
            response.setFavoriteTeam(favoriteTeamResponse);
        }
        
        return response;
    }
}
