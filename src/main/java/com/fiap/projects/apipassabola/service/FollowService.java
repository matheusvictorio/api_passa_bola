package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.FollowRequest;
import com.fiap.projects.apipassabola.dto.FollowResponse;
import com.fiap.projects.apipassabola.entity.*;
import com.fiap.projects.apipassabola.repository.OrganizationRepository;
import com.fiap.projects.apipassabola.repository.PlayerRepository;
import com.fiap.projects.apipassabola.repository.SpectatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FollowService {
    
    private final PlayerRepository playerRepository;
    private final OrganizationRepository organizationRepository;
    private final SpectatorRepository spectatorRepository;
    private final UserContextService userContextService;
    
    @Transactional
    public String followUser(FollowRequest request) {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        // Validar se não está tentando seguir a si mesmo
        if (currentUser.getUserId().equals(request.getTargetUserId()) && 
            currentUser.getUserType().equals(request.getTargetUserType())) {
            throw new RuntimeException("You cannot follow yourself");
        }
        
        // Verificar se o usuário alvo existe
        if (!userExists(request.getTargetUserId(), request.getTargetUserType())) {
            throw new RuntimeException("Target user not found");
        }
        
        // Verificar se já está seguindo
        if (isFollowing(currentUser.getUserId(), currentUser.getUserType(), request.getTargetUserId(), request.getTargetUserType())) {
            throw new RuntimeException("You are already following this user");
        }
        
        // Executar o seguimento baseado nos tipos
        executeFollow(currentUser.getUserId(), currentUser.getUserType(), request.getTargetUserId(), request.getTargetUserType());
        
        return "Successfully followed user";
    }
    
    @Transactional
    public String unfollowUser(FollowRequest request) {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        // Verificar se está seguindo
        if (!isFollowing(currentUser.getUserId(), currentUser.getUserType(), request.getTargetUserId(), request.getTargetUserType())) {
            throw new RuntimeException("You are not following this user");
        }
        
        // Executar o unfollow baseado nos tipos
        executeUnfollow(currentUser.getUserId(), currentUser.getUserType(), request.getTargetUserId(), request.getTargetUserType());
        
        return "Successfully unfollowed user";
    }
    
    public boolean isFollowing(Long followerId, UserType followerType, Long targetId, UserType targetType) {
        switch (followerType) {
            case PLAYER:
                switch (targetType) {
                    case PLAYER:
                        // Player following Player - usar relacionamento direto
                        Player followerPlayer = playerRepository.findById(followerId).orElse(null);
                        Player targetPlayer = playerRepository.findById(targetId).orElse(null);
                        return followerPlayer != null && targetPlayer != null && 
                               followerPlayer.getFollowing().contains(targetPlayer);
                    case ORGANIZATION:
                        // Check both followingOrganizations (correct) and favoriteOrganizations (legacy)
                        // for backward compatibility with potentially inconsistent data
                        Player player = playerRepository.findById(followerId).orElse(null);
                        Organization org = organizationRepository.findById(targetId).orElse(null);
                        
                        if (player != null && org != null) {
                            // First check the correct relationship
                            boolean isFollowing = playerRepository.isFollowingOrganization(followerId, targetId);
                            
                            // If not found in followingOrganizations, check favoriteOrganizations for legacy data
                            if (!isFollowing && player.getFavoriteOrganizations() != null) {
                                isFollowing = player.getFavoriteOrganizations().contains(org);
                            }
                            
                            return isFollowing;
                        }
                        return false;
                    case SPECTATOR:
                        return playerRepository.isFollowingSpectator(followerId, targetId);
                }
                break;
                
            case ORGANIZATION:
                switch (targetType) {
                    case PLAYER:
                        return organizationRepository.isFollowingPlayer(followerId, targetId);
                    case ORGANIZATION:
                        return organizationRepository.isFollowingOrganization(followerId, targetId);
                    case SPECTATOR:
                        return organizationRepository.isFollowingSpectator(followerId, targetId);
                }
                break;
                
            case SPECTATOR:
                switch (targetType) {
                    case PLAYER:
                        return spectatorRepository.isFollowingPlayer(followerId, targetId);
                    case ORGANIZATION:
                        return spectatorRepository.isFollowingOrganization(followerId, targetId);
                    case SPECTATOR:
                        return spectatorRepository.isFollowing(followerId, targetId);
                }
                break;
        }
        
        return false;
    }
    
    public Page<FollowResponse> getFollowers(Long userId, UserType userType, Pageable pageable) {
        switch (userType) {
            case PLAYER:
                // Buscar todos os tipos que seguem este player
                Page<Player> playerFollowers = playerRepository.findFollowersByPlayerId(userId, pageable);
                Page<Organization> orgFollowers = playerRepository.findOrganizationFollowersByPlayerId(userId, pageable);
                Page<Spectator> spectatorFollowers = playerRepository.findSpectatorFollowersByPlayerId(userId, pageable);
                
                // Combinar e converter para FollowResponse
                return playerFollowers.map(this::convertPlayerToFollowResponse);
                
            case ORGANIZATION:
                Page<Player> playerFollowersOrg = organizationRepository.findPlayerFollowersByOrganizationId(userId, pageable);
                return playerFollowersOrg.map(this::convertPlayerToFollowResponse);
                
            case SPECTATOR:
                Page<Spectator> spectatorFollowersSpec = spectatorRepository.findFollowersBySpectatorId(userId, pageable);
                return spectatorFollowersSpec.map(this::convertSpectatorToFollowResponse);
                
            default:
                throw new RuntimeException("Invalid user type");
        }
    }
    
    public Page<FollowResponse> getFollowing(Long userId, UserType userType, Pageable pageable) {
        switch (userType) {
            case PLAYER:
                // Para Player, buscar todos os tipos que ele segue
                // Vamos tentar buscar cada tipo e retornar o primeiro que tem conteúdo
                // Mas agora de forma mais robusta
                
                // 1. Buscar players que ele segue
                Page<Player> followingPlayers = playerRepository.findFollowingByPlayerId(userId, pageable);
                if (followingPlayers.hasContent()) {
                    return followingPlayers.map(this::convertPlayerToFollowResponse);
                }
                
                // 2. Buscar organizações que ele segue
                Page<Organization> followingOrgs = playerRepository.findFollowingOrganizationsByPlayerId(userId, pageable);
                if (followingOrgs.hasContent()) {
                    return followingOrgs.map(this::convertOrganizationToFollowResponse);
                }
                
                // 3. Buscar spectators que ele segue
                Page<Spectator> followingSpectators = playerRepository.findFollowingSpectatorsByPlayerId(userId, pageable);
                return followingSpectators.map(this::convertSpectatorToFollowResponse);
                
            case ORGANIZATION:
                // Para Organization, buscar todos os tipos que ela segue
                
                // 1. Buscar players que ela segue
                Page<Player> followingPlayersOrg = organizationRepository.findFollowingPlayersByOrganizationId(userId, pageable);
                if (followingPlayersOrg.hasContent()) {
                    return followingPlayersOrg.map(this::convertPlayerToFollowResponse);
                }
                
                // 2. Buscar spectators que ela segue
                Page<Spectator> followingSpectatorsOrg = organizationRepository.findFollowingSpectatorsByOrganizationId(userId, pageable);
                if (followingSpectatorsOrg.hasContent()) {
                    return followingSpectatorsOrg.map(this::convertSpectatorToFollowResponse);
                }
                
                // 3. Buscar outras organizações que ela segue
                Page<Organization> followingOrganizations = organizationRepository.findFollowingOrganizationsByOrganizationId(userId, pageable);
                return followingOrganizations.map(this::convertOrganizationToFollowResponse);
                
            case SPECTATOR:
                // Para Spectator, buscar todos os tipos que ele segue
                
                // 1. Buscar players que ele segue
                Page<Player> followingPlayersSpec = spectatorRepository.findFollowingPlayersBySpectatorId(userId, pageable);
                if (followingPlayersSpec.hasContent()) {
                    return followingPlayersSpec.map(this::convertPlayerToFollowResponse);
                }
                
                // 2. Buscar organizações que ele segue
                Page<Organization> followingOrgsSpec = spectatorRepository.findFollowingOrganizationsBySpectatorId(userId, pageable);
                if (followingOrgsSpec.hasContent()) {
                    return followingOrgsSpec.map(this::convertOrganizationToFollowResponse);
                }
                
                // 3. Buscar outros spectators que ele segue
                Page<Spectator> followingSpectatorsSpec = spectatorRepository.findFollowingBySpectatorId(userId, pageable);
                return followingSpectatorsSpec.map(this::convertSpectatorToFollowResponse);
                
            default:
                throw new RuntimeException("Invalid user type");
        }
    }
    
    public boolean userExists(Long userId, UserType userType) {
        switch (userType) {
            case PLAYER:
                return playerRepository.existsById(userId);
            case ORGANIZATION:
                return organizationRepository.existsById(userId);
            case SPECTATOR:
                return spectatorRepository.existsById(userId);
            default:
                return false;
        }
    }
    
    private void executeFollow(Long followerId, UserType followerType, Long targetId, UserType targetType) {
        switch (followerType) {
            case PLAYER:
                Player followerPlayer = playerRepository.findById(followerId)
                    .orElseThrow(() -> new RuntimeException("Follower player not found"));
                
                switch (targetType) {
                    case PLAYER:
                        Player targetPlayer = playerRepository.findById(targetId)
                            .orElseThrow(() -> new RuntimeException("Target player not found"));
                        followerPlayer.getFollowing().add(targetPlayer);
                        targetPlayer.getFollowers().add(followerPlayer);
                        playerRepository.save(followerPlayer);
                        playerRepository.save(targetPlayer);
                        break;
                    case ORGANIZATION:
                        Organization targetOrg = organizationRepository.findById(targetId)
                            .orElseThrow(() -> new RuntimeException("Target organization not found"));
                        followerPlayer.getFollowingOrganizations().add(targetOrg);
                        targetOrg.getPlayerFollowers().add(followerPlayer);
                        playerRepository.save(followerPlayer);
                        organizationRepository.save(targetOrg);
                        break;
                    case SPECTATOR:
                        Spectator targetSpectator = spectatorRepository.findById(targetId)
                            .orElseThrow(() -> new RuntimeException("Target spectator not found"));
                        followerPlayer.getFollowingSpectators().add(targetSpectator);
                        targetSpectator.getPlayerFollowers().add(followerPlayer);
                        playerRepository.save(followerPlayer);
                        spectatorRepository.save(targetSpectator);
                        break;
                }
                break;
                
            case ORGANIZATION:
                Organization followerOrg = organizationRepository.findById(followerId)
                    .orElseThrow(() -> new RuntimeException("Follower organization not found"));
                
                switch (targetType) {
                    case PLAYER:
                        Player targetPlayer = playerRepository.findById(targetId)
                            .orElseThrow(() -> new RuntimeException("Target player not found"));
                        followerOrg.getFollowingPlayers().add(targetPlayer);
                        targetPlayer.getOrganizationFollowers().add(followerOrg);
                        organizationRepository.save(followerOrg);
                        playerRepository.save(targetPlayer);
                        break;
                    case ORGANIZATION:
                        Organization targetOrg = organizationRepository.findById(targetId)
                            .orElseThrow(() -> new RuntimeException("Target organization not found"));
                        followerOrg.getFollowing().add(targetOrg);
                        targetOrg.getFollowers().add(followerOrg);
                        organizationRepository.save(followerOrg);
                        organizationRepository.save(targetOrg);
                        break;
                    case SPECTATOR:
                        Spectator targetSpectator = spectatorRepository.findById(targetId)
                            .orElseThrow(() -> new RuntimeException("Target spectator not found"));
                        followerOrg.getFollowingSpectators().add(targetSpectator);
                        targetSpectator.getOrganizationFollowers().add(followerOrg);
                        organizationRepository.save(followerOrg);
                        spectatorRepository.save(targetSpectator);
                        break;
                }
                break;
                
            case SPECTATOR:
                Spectator followerSpectator = spectatorRepository.findById(followerId)
                    .orElseThrow(() -> new RuntimeException("Follower spectator not found"));
                
                switch (targetType) {
                    case PLAYER:
                        try {
                            Player targetPlayer = playerRepository.findById(targetId)
                                .orElseThrow(() -> new RuntimeException("Target player not found"));
                            
                            // Check if the relationship already exists to prevent duplicates
                            if (followerSpectator.getFollowingPlayers().contains(targetPlayer)) {
                                throw new RuntimeException("Already following this player");
                            }
                            
                            followerSpectator.getFollowingPlayers().add(targetPlayer);
                            targetPlayer.getSpectatorFollowers().add(followerSpectator);
                            spectatorRepository.save(followerSpectator);
                            playerRepository.save(targetPlayer);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to follow player: " + e.getMessage(), e);
                        }
                        break;
                    case ORGANIZATION:
                        try {
                            Organization targetOrg = organizationRepository.findById(targetId)
                                .orElseThrow(() -> new RuntimeException("Target organization not found"));
                            
                            // Check if the relationship already exists to prevent duplicates
                            if (followerSpectator.getFollowingOrganizations().contains(targetOrg)) {
                                throw new RuntimeException("Already following this organization");
                            }
                            
                            followerSpectator.getFollowingOrganizations().add(targetOrg);
                            targetOrg.getSpectatorFollowers().add(followerSpectator);
                            spectatorRepository.save(followerSpectator);
                            organizationRepository.save(targetOrg);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to follow organization: " + e.getMessage(), e);
                        }
                        break;
                    case SPECTATOR:
                        try {
                            Spectator targetSpectator = spectatorRepository.findById(targetId)
                                .orElseThrow(() -> new RuntimeException("Target spectator not found"));
                            
                            // Check if the relationship already exists to prevent duplicates
                            if (followerSpectator.getFollowing().contains(targetSpectator)) {
                                throw new RuntimeException("Already following this spectator");
                            }
                            
                            followerSpectator.getFollowing().add(targetSpectator);
                            targetSpectator.getFollowers().add(followerSpectator);
                            spectatorRepository.save(followerSpectator);
                            spectatorRepository.save(targetSpectator);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to follow spectator: " + e.getMessage(), e);
                        }
                        break;
                }
                break;
        }
    }
    
    private void executeUnfollow(Long followerId, UserType followerType, Long targetId, UserType targetType) {
        switch (followerType) {
            case PLAYER:
                Player followerPlayer = playerRepository.findById(followerId)
                    .orElseThrow(() -> new RuntimeException("Follower player not found"));
                
                switch (targetType) {
                    case PLAYER:
                        Player targetPlayer = playerRepository.findById(targetId)
                            .orElseThrow(() -> new RuntimeException("Target player not found"));
                        followerPlayer.getFollowing().remove(targetPlayer);
                        targetPlayer.getFollowers().remove(followerPlayer);
                        playerRepository.save(followerPlayer);
                        playerRepository.save(targetPlayer);
                        break;
                    case ORGANIZATION:
                        Organization targetOrg = organizationRepository.findById(targetId)
                            .orElseThrow(() -> new RuntimeException("Target organization not found"));
                        followerPlayer.getFollowingOrganizations().remove(targetOrg);
                        targetOrg.getPlayerFollowers().remove(followerPlayer);
                        playerRepository.save(followerPlayer);
                        organizationRepository.save(targetOrg);
                        break;
                    case SPECTATOR:
                        Spectator targetSpectator = spectatorRepository.findById(targetId)
                            .orElseThrow(() -> new RuntimeException("Target spectator not found"));
                        followerPlayer.getFollowingSpectators().remove(targetSpectator);
                        targetSpectator.getPlayerFollowers().remove(followerPlayer);
                        playerRepository.save(followerPlayer);
                        spectatorRepository.save(targetSpectator);
                        break;
                }
                break;
                
            case ORGANIZATION:
                Organization followerOrg = organizationRepository.findById(followerId)
                    .orElseThrow(() -> new RuntimeException("Follower organization not found"));
                
                switch (targetType) {
                    case PLAYER:
                        Player targetPlayer = playerRepository.findById(targetId)
                            .orElseThrow(() -> new RuntimeException("Target player not found"));
                        followerOrg.getFollowingPlayers().remove(targetPlayer);
                        targetPlayer.getOrganizationFollowers().remove(followerOrg);
                        organizationRepository.save(followerOrg);
                        playerRepository.save(targetPlayer);
                        break;
                    case ORGANIZATION:
                        Organization targetOrg = organizationRepository.findById(targetId)
                            .orElseThrow(() -> new RuntimeException("Target organization not found"));
                        followerOrg.getFollowing().remove(targetOrg);
                        targetOrg.getFollowers().remove(followerOrg);
                        organizationRepository.save(followerOrg);
                        organizationRepository.save(targetOrg);
                        break;
                    case SPECTATOR:
                        Spectator targetSpectator = spectatorRepository.findById(targetId)
                            .orElseThrow(() -> new RuntimeException("Target spectator not found"));
                        followerOrg.getFollowingSpectators().remove(targetSpectator);
                        targetSpectator.getOrganizationFollowers().remove(followerOrg);
                        organizationRepository.save(followerOrg);
                        spectatorRepository.save(targetSpectator);
                        break;
                }
                break;
                
            case SPECTATOR:
                Spectator followerSpectator = spectatorRepository.findById(followerId)
                    .orElseThrow(() -> new RuntimeException("Follower spectator not found"));
                
                switch (targetType) {
                    case PLAYER:
                        Player targetPlayer = playerRepository.findById(targetId)
                            .orElseThrow(() -> new RuntimeException("Target player not found"));
                        followerSpectator.getFollowingPlayers().remove(targetPlayer);
                        targetPlayer.getSpectatorFollowers().remove(followerSpectator);
                        spectatorRepository.save(followerSpectator);
                        playerRepository.save(targetPlayer);
                        break;
                    case ORGANIZATION:
                        Organization targetOrg = organizationRepository.findById(targetId)
                            .orElseThrow(() -> new RuntimeException("Target organization not found"));
                        followerSpectator.getFollowingOrganizations().remove(targetOrg);
                        targetOrg.getSpectatorFollowers().remove(followerSpectator);
                        spectatorRepository.save(followerSpectator);
                        organizationRepository.save(targetOrg);
                        break;
                    case SPECTATOR:
                        Spectator targetSpectator = spectatorRepository.findById(targetId)
                            .orElseThrow(() -> new RuntimeException("Target spectator not found"));
                        followerSpectator.getFollowing().remove(targetSpectator);
                        targetSpectator.getFollowers().remove(followerSpectator);
                        spectatorRepository.save(followerSpectator);
                        spectatorRepository.save(targetSpectator);
                        break;
                }
                break;
        }
    }
    
    /**
     * Migrates data from favoriteOrganizations to followingOrganizations for all players.
     * This method is useful for ensuring backward compatibility with legacy data.
     * It should be called once during application startup or as a maintenance task.
     */
    @Transactional
    public void migratePlayerFavoriteOrganizationsToFollowing() {
        List<Player> allPlayers = playerRepository.findAll();
        int migratedCount = 0;
        
        for (Player player : allPlayers) {
            if (player.getFavoriteOrganizations() != null && !player.getFavoriteOrganizations().isEmpty()) {
                Set<Organization> favorites = new HashSet<>(player.getFavoriteOrganizations());
                
                for (Organization org : favorites) {
                    // Check if the organization is already in the following list
                    if (!player.getFollowingOrganizations().contains(org)) {
                        // Add to followingOrganizations
                        player.getFollowingOrganizations().add(org);
                        
                        // Add player to organization's followers if not already there
                        if (!org.getPlayerFollowers().contains(player)) {
                            org.getPlayerFollowers().add(player);
                            organizationRepository.save(org);
                        }
                        
                        migratedCount++;
                    }
                }
                
                // Save the player with updated relationships
                playerRepository.save(player);
            }
        }
        
        System.out.println("Migration completed: " + migratedCount + " favorite organizations migrated to following relationships");
    }
    
    private FollowResponse convertPlayerToFollowResponse(Player player) {
        FollowResponse response = new FollowResponse();
        response.setId(player.getId());
        response.setUsername(player.getRealUsername());
        response.setName(player.getName());
        response.setEmail(player.getEmail());
        response.setUserType(player.getUserType());
        response.setBio(player.getBio());
        response.setProfilePhotoUrl(player.getProfilePhotoUrl());
        response.setBannerUrl(player.getBannerUrl());
        response.setPhone(player.getPhone());
        response.setCreatedAt(player.getCreatedAt());
        
        // Campos específicos de Player
        if (player.getBirthDate() != null) {
            response.setBirthDate(player.getBirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        // Remover campos que não existem na entidade atual
        // response.setOrganizationId(player.getOrganizationId());
        // response.setPastOrganization(player.getPastOrganization());
        
        return response;
    }
    
    private FollowResponse convertOrganizationToFollowResponse(Organization organization) {
        FollowResponse response = new FollowResponse();
        response.setId(organization.getId());
        response.setUsername(organization.getRealUsername());
        response.setName(organization.getName());
        response.setEmail(organization.getEmail());
        response.setUserType(organization.getUserType());
        response.setBio(organization.getBio());
        response.setProfilePhotoUrl(organization.getProfilePhotoUrl());
        response.setBannerUrl(organization.getBannerUrl());
        response.setPhone(organization.getPhone());
        response.setCreatedAt(organization.getCreatedAt());
        
        // Campos específicos de Organization
        response.setCnpj(organization.getCnpj());
        response.setCity(organization.getCity());
        response.setState(organization.getState());
        
        return response;
    }
    
    private FollowResponse convertSpectatorToFollowResponse(Spectator spectator) {
        FollowResponse response = new FollowResponse();
        response.setId(spectator.getId());
        response.setUsername(spectator.getRealUsername());
        response.setName(spectator.getName());
        response.setEmail(spectator.getEmail());
        response.setUserType(spectator.getUserType());
        response.setBio(spectator.getBio());
        response.setProfilePhotoUrl(spectator.getProfilePhotoUrl());
        response.setBannerUrl(spectator.getBannerUrl());
        response.setPhone(spectator.getPhone());
        response.setCreatedAt(spectator.getCreatedAt());
        
        // Campos específicos de Spectator
        if (spectator.getBirthDate() != null) {
            response.setBirthDate(spectator.getBirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        // Remover campo que não existe na entidade atual
        // response.setFavoriteTeamId(spectator.getFavoriteTeamId());
        
        return response;
    }
}
