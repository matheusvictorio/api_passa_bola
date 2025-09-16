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
        Player player = playerRepository.findByUsername(username)
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
    
    
    public PlayerResponse update(Long id, PlayerRequest request) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", id));
        
        
        player.setUsername(request.getUsername());
        player.setName(request.getName());
        player.setEmail(request.getEmail());
        player.setBio(request.getBio());
        player.setBirthDate(request.getBirthDate());
        player.setProfilePhotoUrl(request.getProfilePhotoUrl());
        player.setBannerUrl(request.getBannerUrl());
        player.setPhone(request.getPhone());
        player.setPastOrganization(request.getPastOrganization());
        player.setGamesPlayed(request.getGamesPlayed());
        
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
    
    public PlayerResponse followByEmail(String followerEmail, Long followedId) {
        Player follower = playerRepository.findByEmail(followerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "email", followerEmail));
        return follow(follower.getId(), followedId);
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
    
    public PlayerResponse unfollowByEmail(String followerEmail, Long followedId) {
        Player follower = playerRepository.findByEmail(followerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Player", "email", followerEmail));
        return unfollow(follower.getId(), followedId);
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
        response.setUserType(player.getUserType());
        response.setUsername(player.getRealUsername()); // Use getRealUsername() to get actual username, not email
        response.setName(player.getName());
        response.setEmail(player.getEmail());
        response.setBio(player.getBio());
        response.setBirthDate(player.getBirthDate());
        response.setProfilePhotoUrl(player.getProfilePhotoUrl());
        response.setBannerUrl(player.getBannerUrl());
        response.setPhone(player.getPhone());
        response.setPastOrganization(player.getPastOrganization());
        response.setGamesPlayed(player.getGamesPlayed());
        response.setFollowersCount(player.getFollowersCount());
        response.setFollowingCount(player.getFollowingCount());
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
    
    public PlayerSummaryResponse convertToSummaryResponse(Player player) {
        PlayerSummaryResponse response = new PlayerSummaryResponse();
        response.setId(player.getId());
        response.setUsername(player.getRealUsername()); // Use getRealUsername() to get actual username, not email
        response.setName(player.getName());
        response.setProfilePhotoUrl(player.getProfilePhotoUrl());
        
        if (player.getOrganization() != null) {
            OrganizationSummaryResponse orgResponse = new OrganizationSummaryResponse();
            orgResponse.setId(player.getOrganization().getId());
            orgResponse.setName(player.getOrganization().getName());
            response.setOrganization(orgResponse);
        }
        
        return response;
    }
}
