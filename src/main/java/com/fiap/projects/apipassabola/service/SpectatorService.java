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
    
    // Following/Followers methods
    
    /**
     * Follow another spectator
     */
    public void followSpectator(Long spectatorToFollowId) {
        Spectator currentSpectator = userContextService.getCurrentSpectator();
        if (currentSpectator == null) {
            throw new RuntimeException("Only spectators can follow other spectators");
        }
        
        if (currentSpectator.getId().equals(spectatorToFollowId)) {
            throw new RuntimeException("Cannot follow yourself");
        }
        
        Spectator spectatorToFollow = spectatorRepository.findById(spectatorToFollowId)
                .orElseThrow(() -> new ResourceNotFoundException("Spectator", "id", spectatorToFollowId));
        
        if (spectatorRepository.isFollowing(currentSpectator.getId(), spectatorToFollowId)) {
            throw new RuntimeException("Already following this spectator");
        }
        
        currentSpectator.getFollowing().add(spectatorToFollow);
        spectatorToFollow.getFollowers().add(currentSpectator);
        
        spectatorRepository.save(currentSpectator);
        spectatorRepository.save(spectatorToFollow);
    }
    
    /**
     * Unfollow a spectator
     */
    public void unfollowSpectator(Long spectatorToUnfollowId) {
        Spectator currentSpectator = userContextService.getCurrentSpectator();
        if (currentSpectator == null) {
            throw new RuntimeException("Only spectators can unfollow other spectators");
        }
        
        Spectator spectatorToUnfollow = spectatorRepository.findById(spectatorToUnfollowId)
                .orElseThrow(() -> new ResourceNotFoundException("Spectator", "id", spectatorToUnfollowId));
        
        if (!spectatorRepository.isFollowing(currentSpectator.getId(), spectatorToUnfollowId)) {
            throw new RuntimeException("Not following this spectator");
        }
        
        currentSpectator.getFollowing().remove(spectatorToUnfollow);
        spectatorToUnfollow.getFollowers().remove(currentSpectator);
        
        spectatorRepository.save(currentSpectator);
        spectatorRepository.save(spectatorToUnfollow);
    }
    
    /**
     * Get followers of a spectator
     */
    public Page<SpectatorResponse> getFollowers(Long spectatorId, Pageable pageable) {
        if (!spectatorRepository.existsById(spectatorId)) {
            throw new ResourceNotFoundException("Spectator", "id", spectatorId);
        }
        
        return spectatorRepository.findFollowersBySpectatorId(spectatorId, pageable)
                .map(this::convertToResponse);
    }
    
    /**
     * Get spectators that a spectator is following
     */
    public Page<SpectatorResponse> getFollowing(Long spectatorId, Pageable pageable) {
        if (!spectatorRepository.existsById(spectatorId)) {
            throw new ResourceNotFoundException("Spectator", "id", spectatorId);
        }
        
        return spectatorRepository.findFollowingBySpectatorId(spectatorId, pageable)
                .map(this::convertToResponse);
    }
    
    /**
     * Check if current spectator is following another spectator
     */
    public boolean isFollowing(Long spectatorId) {
        Spectator currentSpectator = userContextService.getCurrentSpectator();
        if (currentSpectator == null) {
            return false;
        }
        
        return spectatorRepository.isFollowing(currentSpectator.getId(), spectatorId);
    }
    
    /**
     * Get my followers (current spectator's followers)
     */
    public Page<SpectatorResponse> getMyFollowers(Pageable pageable) {
        Spectator currentSpectator = userContextService.getCurrentSpectator();
        if (currentSpectator == null) {
            throw new RuntimeException("Only spectators can view their followers");
        }
        
        return getFollowers(currentSpectator.getId(), pageable);
    }
    
    /**
     * Get who I'm following (current spectator's following list)
     */
    public Page<SpectatorResponse> getMyFollowing(Pageable pageable) {
        Spectator currentSpectator = userContextService.getCurrentSpectator();
        if (currentSpectator == null) {
            throw new RuntimeException("Only spectators can view their following list");
        }
        
        return getFollowing(currentSpectator.getId(), pageable);
    }
    
    // Cross-type following methods
    
    /**
     * Follow a player
     */
    public void followPlayer(Long playerId) {
        Spectator currentSpectator = userContextService.getCurrentSpectator();
        if (currentSpectator == null) {
            throw new RuntimeException("Only spectators can follow players");
        }
        
        Player playerToFollow = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", playerId));
        
        if (spectatorRepository.isFollowingPlayer(currentSpectator.getId(), playerId)) {
            throw new RuntimeException("Already following this player");
        }
        
        currentSpectator.getFollowingPlayers().add(playerToFollow);
        playerToFollow.getSpectatorFollowers().add(currentSpectator);
        
        spectatorRepository.save(currentSpectator);
        playerRepository.save(playerToFollow);
    }
    
    /**
     * Unfollow a player
     */
    public void unfollowPlayer(Long playerId) {
        Spectator currentSpectator = userContextService.getCurrentSpectator();
        if (currentSpectator == null) {
            throw new RuntimeException("Only spectators can unfollow players");
        }
        
        Player playerToUnfollow = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", playerId));
        
        if (!spectatorRepository.isFollowingPlayer(currentSpectator.getId(), playerId)) {
            throw new RuntimeException("Not following this player");
        }
        
        currentSpectator.getFollowingPlayers().remove(playerToUnfollow);
        playerToUnfollow.getSpectatorFollowers().remove(currentSpectator);
        
        spectatorRepository.save(currentSpectator);
        playerRepository.save(playerToUnfollow);
    }
    
    /**
     * Follow an organization
     */
    public void followOrganization(Long organizationId) {
        Spectator currentSpectator = userContextService.getCurrentSpectator();
        if (currentSpectator == null) {
            throw new RuntimeException("Only spectators can follow organizations");
        }
        
        Organization organizationToFollow = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
        
        if (spectatorRepository.isFollowingOrganization(currentSpectator.getId(), organizationId)) {
            throw new RuntimeException("Already following this organization");
        }
        
        currentSpectator.getFollowingOrganizations().add(organizationToFollow);
        organizationToFollow.getSpectatorFollowers().add(currentSpectator);
        
        spectatorRepository.save(currentSpectator);
        organizationRepository.save(organizationToFollow);
    }
    
    /**
     * Unfollow an organization
     */
    public void unfollowOrganization(Long organizationId) {
        Spectator currentSpectator = userContextService.getCurrentSpectator();
        if (currentSpectator == null) {
            throw new RuntimeException("Only spectators can unfollow organizations");
        }
        
        Organization organizationToUnfollow = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
        
        if (!spectatorRepository.isFollowingOrganization(currentSpectator.getId(), organizationId)) {
            throw new RuntimeException("Not following this organization");
        }
        
        currentSpectator.getFollowingOrganizations().remove(organizationToUnfollow);
        organizationToUnfollow.getSpectatorFollowers().remove(currentSpectator);
        
        spectatorRepository.save(currentSpectator);
        organizationRepository.save(organizationToUnfollow);
    }
    
    /**
     * Get players that a spectator is following
     */
    public Page<PlayerResponse> getFollowingPlayers(Long spectatorId, Pageable pageable) {
        if (!spectatorRepository.existsById(spectatorId)) {
            throw new ResourceNotFoundException("Spectator", "id", spectatorId);
        }
        
        return spectatorRepository.findFollowingPlayersBySpectatorId(spectatorId, pageable)
                .map(this::convertPlayerToResponse);
    }
    
    /**
     * Get organizations that a spectator is following
     */
    public Page<OrganizationResponse> getFollowingOrganizations(Long spectatorId, Pageable pageable) {
        if (!spectatorRepository.existsById(spectatorId)) {
            throw new ResourceNotFoundException("Spectator", "id", spectatorId);
        }
        
        return spectatorRepository.findFollowingOrganizationsBySpectatorId(spectatorId, pageable)
                .map(this::convertOrganizationToResponse);
    }
    
    /**
     * Check if current spectator is following a player
     */
    public boolean isFollowingPlayer(Long playerId) {
        Spectator currentSpectator = userContextService.getCurrentSpectator();
        if (currentSpectator == null) {
            return false;
        }
        
        return spectatorRepository.isFollowingPlayer(currentSpectator.getId(), playerId);
    }
    
    /**
     * Check if current spectator is following an organization
     */
    public boolean isFollowingOrganization(Long organizationId) {
        Spectator currentSpectator = userContextService.getCurrentSpectator();
        if (currentSpectator == null) {
            return false;
        }
        
        return spectatorRepository.isFollowingOrganization(currentSpectator.getId(), organizationId);
    }
    
    /**
     * Get players I'm following (current spectator's following players list)
     */
    public Page<PlayerResponse> getMyFollowingPlayers(Pageable pageable) {
        Spectator currentSpectator = userContextService.getCurrentSpectator();
        if (currentSpectator == null) {
            throw new RuntimeException("Only spectators can view their following players list");
        }
        
        return getFollowingPlayers(currentSpectator.getId(), pageable);
    }
    
    /**
     * Get organizations I'm following (current spectator's following organizations list)
     */
    public Page<OrganizationResponse> getMyFollowingOrganizations(Pageable pageable) {
        Spectator currentSpectator = userContextService.getCurrentSpectator();
        if (currentSpectator == null) {
            throw new RuntimeException("Only spectators can view their following organizations list");
        }
        
        return getFollowingOrganizations(currentSpectator.getId(), pageable);
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
    
    private PlayerResponse convertPlayerToResponse(Player player) {
        PlayerResponse response = new PlayerResponse();
        response.setId(player.getId());
        response.setUserType(player.getUserType());
        response.setUsername(player.getRealUsername());
        response.setName(player.getName());
        response.setEmail(player.getEmail());
        response.setBio(player.getBio());
        response.setBirthDate(player.getBirthDate());
        response.setPhone(player.getPhone());
        response.setProfilePhotoUrl(player.getProfilePhotoUrl());
        response.setBannerUrl(player.getBannerUrl());
        response.setFollowersCount(player.getFollowersCount());
        response.setFollowingCount(player.getFollowingCount());
        response.setGamesPlayed(player.getGamesPlayed());
        response.setCreatedAt(player.getCreatedAt());
        response.setUpdatedAt(player.getUpdatedAt());
        
        if (player.getOrganization() != null) {
            OrganizationSummaryResponse organizationSummary = new OrganizationSummaryResponse();
            organizationSummary.setId(player.getOrganization().getId());
            organizationSummary.setName(player.getOrganization().getName());
            organizationSummary.setLogoUrl(player.getOrganization().getProfilePhotoUrl());
            organizationSummary.setCity(player.getOrganization().getCity());
            organizationSummary.setState(player.getOrganization().getState());
            response.setOrganization(organizationSummary);
        }
        response.setPastOrganization(player.getPastOrganization());
        
        return response;
    }
    
    private OrganizationResponse convertOrganizationToResponse(Organization organization) {
        OrganizationResponse response = new OrganizationResponse();
        response.setId(organization.getId());
        response.setUserType(organization.getUserType());
        response.setUsername(organization.getRealUsername());
        response.setName(organization.getName());
        response.setEmail(organization.getEmail());
        response.setCnpj(organization.getCnpj());
        response.setBio(organization.getBio());
        response.setPhone(organization.getPhone());
        response.setProfilePhotoUrl(organization.getProfilePhotoUrl());
        response.setBannerUrl(organization.getBannerUrl());
        response.setCity(organization.getCity());
        response.setState(organization.getState());
        response.setFollowersCount(organization.getFollowersCount());
        response.setFollowingCount(organization.getFollowingCount());
        response.setGamesPlayed(organization.getGamesPlayed());
        response.setCreatedAt(organization.getCreatedAt());
        response.setUpdatedAt(organization.getUpdatedAt());
        
        return response;
    }
}
