package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.request.OrganizationRequest;
import com.fiap.projects.apipassabola.dto.response.OrganizationResponse;
import com.fiap.projects.apipassabola.dto.response.OrganizationSummaryResponse;
import com.fiap.projects.apipassabola.dto.response.PlayerResponse;
import com.fiap.projects.apipassabola.dto.response.SpectatorResponse;
import com.fiap.projects.apipassabola.entity.Organization;
import com.fiap.projects.apipassabola.entity.Player;
import com.fiap.projects.apipassabola.entity.Spectator;
import com.fiap.projects.apipassabola.exception.BusinessException;
import com.fiap.projects.apipassabola.exception.ResourceNotFoundException;
import com.fiap.projects.apipassabola.repository.GameRepository;
import com.fiap.projects.apipassabola.repository.OrganizationRepository;
import com.fiap.projects.apipassabola.repository.PlayerRepository;
import com.fiap.projects.apipassabola.repository.SpectatorRepository;
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
    private final SpectatorRepository spectatorRepository;
    
    public Page<OrganizationResponse> findAll(Pageable pageable) {
        return organizationRepository.findAll(pageable).map(this::convertToResponse);
    }
    
    public OrganizationResponse findById(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        return convertToResponse(organization);
    }
    
    public OrganizationResponse findByUsername(String username) {
        Organization organization = organizationRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "username", username));
        return convertToResponse(organization);
    }
    
    public Page<OrganizationResponse> findByName(String name, Pageable pageable) {
        return organizationRepository.findByNameContainingIgnoreCase(name, pageable)
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
        
        organization.setUsername(request.getUsername());
        organization.setName(request.getName());
        organization.setEmail(request.getEmail());
        organization.setBio(request.getBio());
        organization.setProfilePhotoUrl(request.getProfilePhotoUrl());
        organization.setBannerUrl(request.getBannerUrl());
        organization.setPhone(request.getPhone());
        organization.setCity(request.getCity());
        organization.setState(request.getState());
        organization.setGamesPlayed(request.getGamesPlayed());
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
        response.setUserType(organization.getUserType());
        response.setUsername(organization.getRealUsername()); // Use getRealUsername() to get actual username, not email
        response.setName(organization.getName());
        response.setEmail(organization.getEmail());
        response.setCnpj(CnpjValidator.format(organization.getCnpj()));
        response.setBio(organization.getBio());
        response.setProfilePhotoUrl(organization.getProfilePhotoUrl());
        response.setBannerUrl(organization.getBannerUrl());
        response.setPhone(organization.getPhone());
        response.setCity(organization.getCity());
        response.setState(organization.getState());
        response.setGamesPlayed(organization.getGamesPlayed());
        response.setFollowersCount(organization.getFollowersCount());
        response.setFollowingCount(organization.getFollowingCount());
        response.setCreatedAt(organization.getCreatedAt());
        response.setUpdatedAt(organization.getUpdatedAt());
        
        return response;
    }
    
    public OrganizationSummaryResponse convertToSummaryResponse(Organization organization) {
        OrganizationSummaryResponse response = new OrganizationSummaryResponse();
        response.setId(organization.getId());
        response.setName(organization.getName());
        response.setLogoUrl(organization.getProfilePhotoUrl()); // Using profilePhotoUrl as logoUrl
        response.setCity(organization.getCity());
        response.setState(organization.getState());
        
        return response;
    }
    
    // ========== CROSS-TYPE FOLLOWING METHODS FOR ORGANIZATION ==========
    
    // Organization following Players
    public void followPlayer(Long organizationId, Long playerId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
        
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", playerId));
        
        if (organizationRepository.isFollowingPlayer(organizationId, playerId)) {
            throw new BusinessException("Organization is already following this player");
        }
        
        organization.getFollowingPlayers().add(player);
        player.getOrganizationFollowers().add(organization);
        
        organizationRepository.save(organization);
        playerRepository.save(player);
    }
    
    public void unfollowPlayer(Long organizationId, Long playerId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
        
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", playerId));
        
        if (!organizationRepository.isFollowingPlayer(organizationId, playerId)) {
            throw new BusinessException("Organization is not following this player");
        }
        
        organization.getFollowingPlayers().remove(player);
        player.getOrganizationFollowers().remove(organization);
        
        organizationRepository.save(organization);
        playerRepository.save(player);
    }
    
    // Organization following Spectators
    public void followSpectator(Long organizationId, Long spectatorId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
        
        Spectator spectator = spectatorRepository.findById(spectatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Spectator", "id", spectatorId));
        
        if (organizationRepository.isFollowingSpectator(organizationId, spectatorId)) {
            throw new BusinessException("Organization is already following this spectator");
        }
        
        organization.getFollowingSpectators().add(spectator);
        spectator.getOrganizationFollowers().add(organization);
        
        organizationRepository.save(organization);
        spectatorRepository.save(spectator);
    }
    
    public void unfollowSpectator(Long organizationId, Long spectatorId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));
        
        Spectator spectator = spectatorRepository.findById(spectatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Spectator", "id", spectatorId));
        
        if (!organizationRepository.isFollowingSpectator(organizationId, spectatorId)) {
            throw new BusinessException("Organization is not following this spectator");
        }
        
        organization.getFollowingSpectators().remove(spectator);
        spectator.getOrganizationFollowers().remove(organization);
        
        organizationRepository.save(organization);
        spectatorRepository.save(spectator);
    }
    
    // Get following lists for Organization
    public Page<PlayerResponse> getFollowingPlayers(Long organizationId, Pageable pageable) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization", "id", organizationId);
        }
        
        return organizationRepository.findFollowingPlayersByOrganizationId(organizationId, pageable)
                .map(this::convertPlayerToResponse);
    }
    
    public Page<SpectatorResponse> getFollowingSpectators(Long organizationId, Pageable pageable) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization", "id", organizationId);
        }
        
        return organizationRepository.findFollowingSpectatorsByOrganizationId(organizationId, pageable)
                .map(this::convertSpectatorToResponse);
    }
    
    // Check following status for Organization
    public boolean isFollowingPlayer(Long organizationId, Long playerId) {
        return organizationRepository.isFollowingPlayer(organizationId, playerId);
    }
    
    public boolean isFollowingSpectator(Long organizationId, Long spectatorId) {
        return organizationRepository.isFollowingSpectator(organizationId, spectatorId);
    }
    
    // Get followers lists for Organization (cross-type)
    public Page<PlayerResponse> getPlayerFollowers(Long organizationId, Pageable pageable) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization", "id", organizationId);
        }
        
        return organizationRepository.findPlayerFollowersByOrganizationId(organizationId, pageable)
                .map(this::convertPlayerToResponse);
    }
    
    public Page<SpectatorResponse> getSpectatorFollowers(Long organizationId, Pageable pageable) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization", "id", organizationId);
        }
        
        return organizationRepository.findSpectatorFollowersByOrganizationId(organizationId, pageable)
                .map(this::convertSpectatorToResponse);
    }
    
    // Organization-to-Organization following methods (existing functionality enhanced)
    public void followOrganization(Long followerId, Long followedId) {
        if (followerId.equals(followedId)) {
            throw new BusinessException("Organization cannot follow itself");
        }
        
        Organization follower = organizationRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", followerId));
        
        Organization followed = organizationRepository.findById(followedId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", followedId));
        
        if (organizationRepository.isFollowingOrganization(followerId, followedId)) {
            throw new BusinessException("Organization is already following this organization");
        }
        
        follower.getFollowing().add(followed);
        followed.getFollowers().add(follower);
        
        organizationRepository.save(follower);
        organizationRepository.save(followed);
    }
    
    public void unfollowOrganization(Long followerId, Long followedId) {
        Organization follower = organizationRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", followerId));
        
        Organization followed = organizationRepository.findById(followedId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", followedId));
        
        if (!organizationRepository.isFollowingOrganization(followerId, followedId)) {
            throw new BusinessException("Organization is not following this organization");
        }
        
        follower.getFollowing().remove(followed);
        followed.getFollowers().remove(follower);
        
        organizationRepository.save(follower);
        organizationRepository.save(followed);
    }
    
    public Page<OrganizationResponse> getFollowers(Long organizationId, Pageable pageable) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization", "id", organizationId);
        }
        
        return organizationRepository.findFollowersByOrganizationId(organizationId, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<OrganizationResponse> getFollowing(Long organizationId, Pageable pageable) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization", "id", organizationId);
        }
        
        return organizationRepository.findFollowingByOrganizationId(organizationId, pageable)
                .map(this::convertToResponse);
    }
    
    public boolean isFollowingOrganization(Long followerId, Long followedId) {
        return organizationRepository.isFollowingOrganization(followerId, followedId);
    }
    
    // Conversion methods for cross-type responses
    private PlayerResponse convertPlayerToResponse(Player player) {
        PlayerResponse response = new PlayerResponse();
        response.setId(player.getId());
        response.setUsername(player.getRealUsername());
        response.setName(player.getName());
        response.setEmail(player.getEmail());
        response.setProfilePhotoUrl(player.getProfilePhotoUrl());
        response.setBio(player.getBio());
        // Note: Position field might not exist in Player entity, removing this line
        response.setCreatedAt(player.getCreatedAt());
        response.setUpdatedAt(player.getUpdatedAt());
        
        if (player.getOrganization() != null) {
            OrganizationSummaryResponse orgResponse = new OrganizationSummaryResponse();
            orgResponse.setId(player.getOrganization().getId());
            orgResponse.setName(player.getOrganization().getName());
            response.setOrganization(orgResponse);
        }
        
        return response;
    }
    
    private SpectatorResponse convertSpectatorToResponse(Spectator spectator) {
        SpectatorResponse response = new SpectatorResponse();
        response.setId(spectator.getId());
        response.setUsername(spectator.getRealUsername());
        response.setName(spectator.getName());
        response.setEmail(spectator.getEmail());
        response.setProfilePhotoUrl(spectator.getProfilePhotoUrl());
        response.setBio(spectator.getBio());
        response.setCreatedAt(spectator.getCreatedAt());
        response.setUpdatedAt(spectator.getUpdatedAt());
        return response;
    }
}
