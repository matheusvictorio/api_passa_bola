package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.request.PlayerRequest;
import com.fiap.projects.apipassabola.dto.response.OrganizationSummaryResponse;
import com.fiap.projects.apipassabola.dto.response.PlayerResponse;
import com.fiap.projects.apipassabola.dto.response.PlayerSummaryResponse;
import com.fiap.projects.apipassabola.entity.Organization;
import com.fiap.projects.apipassabola.entity.Player;
import com.fiap.projects.apipassabola.exception.BusinessException;
import com.fiap.projects.apipassabola.exception.ResourceNotFoundException;
import com.fiap.projects.apipassabola.repository.OrganizationRepository;
import com.fiap.projects.apipassabola.repository.PlayerRepository;
import com.fiap.projects.apipassabola.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PlayerService {
    
    private final PlayerRepository playerRepository;
    private final OrganizationRepository organizationRepository;
    private final PostRepository postRepository;
    
    public Page<PlayerResponse> findAll(Pageable pageable) {
        return playerRepository.findAll(pageable).map(this::convertToResponse);
    }
    
    public PlayerResponse findById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", id));
        return convertToResponse(player);
    }
    
    public PlayerResponse findByUsername(String username) {
        Player player = playerRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "username", username));
        return convertToResponse(player);
    }
    
    public Page<PlayerResponse> findByName(String name, Pageable pageable) {
        return playerRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<PlayerResponse> findByOrganization(Long organizationId, Pageable pageable) {
        return playerRepository.findByOrganizationId(organizationId, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<PlayerResponse> findByPosition(Player.Position position, Pageable pageable) {
        return playerRepository.findByPosition(position, pageable)
                .map(this::convertToResponse);
    }
    
    public PlayerResponse update(Long id, PlayerRequest request) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", id));
        
        // Validate jersey number uniqueness within organization
        if (request.getJerseyNumber() != null && request.getOrganizationId() != null) {
            playerRepository.findByJerseyNumberAndOrganizationId(request.getJerseyNumber(), request.getOrganizationId())
                    .filter(p -> !p.getId().equals(id))
                    .ifPresent(p -> {
                        throw new BusinessException("Jersey number " + request.getJerseyNumber() + " is already taken in this organization");
                    });
        }
        
        player.setFirstName(request.getFirstName());
        player.setLastName(request.getLastName());
        player.setBio(request.getBio());
        player.setBirthDate(request.getBirthDate());
        player.setPosition(request.getPosition());
        player.setProfilePhotoUrl(request.getProfilePhotoUrl());
        player.setJerseyNumber(request.getJerseyNumber());
        
        if (request.getOrganizationId() != null) {
            Organization organization = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));
            player.setOrganization(organization);
        } else {
            player.setOrganization(null);
        }
        
        Player savedPlayer = playerRepository.save(player);
        return convertToResponse(savedPlayer);
    }
    
    public void delete(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Player", "id", id);
        }
        playerRepository.deleteById(id);
    }
    
    public PlayerResponse follow(Long followerId, Long followedId) {
        if (followerId.equals(followedId)) {
            throw new BusinessException("Player cannot follow themselves");
        }
        
        Player follower = playerRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", followerId));
        Player followed = playerRepository.findById(followedId)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", followedId));
        
        if (followed.getFollowers().contains(follower)) {
            throw new BusinessException("Player is already following this player");
        }
        
        followed.getFollowers().add(follower);
        follower.getFollowing().add(followed);
        
        playerRepository.save(followed);
        playerRepository.save(follower);
        
        return convertToResponse(followed);
    }
    
    public PlayerResponse unfollow(Long followerId, Long followedId) {
        Player follower = playerRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", followerId));
        Player followed = playerRepository.findById(followedId)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", followedId));
        
        if (!followed.getFollowers().contains(follower)) {
            throw new BusinessException("Player is not following this player");
        }
        
        followed.getFollowers().remove(follower);
        follower.getFollowing().remove(followed);
        
        playerRepository.save(followed);
        playerRepository.save(follower);
        
        return convertToResponse(followed);
    }
    
    public Page<PlayerResponse> getFollowers(Long playerId, Pageable pageable) {
        return playerRepository.findFollowersByPlayerId(playerId, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<PlayerResponse> getFollowing(Long playerId, Pageable pageable) {
        return playerRepository.findFollowedPlayersByFollowerId(playerId, pageable)
                .map(this::convertToResponse);
    }
    
    private PlayerResponse convertToResponse(Player player) {
        PlayerResponse response = new PlayerResponse();
        response.setId(player.getId());
        response.setUserId(player.getUser().getId());
        response.setUsername(player.getUser().getUsername());
        response.setEmail(player.getUser().getEmail());
        response.setFirstName(player.getFirstName());
        response.setLastName(player.getLastName());
        response.setFullName(player.getFullName());
        response.setBio(player.getBio());
        response.setBirthDate(player.getBirthDate());
        response.setPosition(player.getPosition());
        response.setProfilePhotoUrl(player.getProfilePhotoUrl());
        response.setJerseyNumber(player.getJerseyNumber());
        response.setFollowersCount(player.getFollowersCount());
        response.setFollowingCount(player.getFollowingCount());
        response.setPostsCount(postRepository.countByPlayerId(player.getId()).intValue());
        response.setCreatedAt(player.getCreatedAt());
        response.setUpdatedAt(player.getUpdatedAt());
        
        if (player.getOrganization() != null) {
            OrganizationSummaryResponse orgResponse = new OrganizationSummaryResponse();
            orgResponse.setId(player.getOrganization().getId());
            orgResponse.setName(player.getOrganization().getName());
            orgResponse.setLogoUrl(player.getOrganization().getLogoUrl());
            orgResponse.setCity(player.getOrganization().getCity());
            orgResponse.setState(player.getOrganization().getState());
            response.setOrganization(orgResponse);
        }
        
        return response;
    }
    
    public PlayerSummaryResponse convertToSummaryResponse(Player player) {
        PlayerSummaryResponse response = new PlayerSummaryResponse();
        response.setId(player.getId());
        response.setUsername(player.getUser().getUsername());
        response.setFirstName(player.getFirstName());
        response.setLastName(player.getLastName());
        response.setFullName(player.getFullName());
        response.setProfilePhotoUrl(player.getProfilePhotoUrl());
        response.setPosition(player.getPosition());
        
        if (player.getOrganization() != null) {
            OrganizationSummaryResponse orgResponse = new OrganizationSummaryResponse();
            orgResponse.setId(player.getOrganization().getId());
            orgResponse.setName(player.getOrganization().getName());
            orgResponse.setLogoUrl(player.getOrganization().getLogoUrl());
            orgResponse.setCity(player.getOrganization().getCity());
            orgResponse.setState(player.getOrganization().getState());
            response.setOrganization(orgResponse);
        }
        
        return response;
    }
}
