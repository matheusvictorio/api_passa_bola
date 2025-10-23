package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.FollowRequest;
import com.fiap.projects.apipassabola.dto.FollowResponse;
import com.fiap.projects.apipassabola.entity.*;
import com.fiap.projects.apipassabola.repository.OrganizationRepository;
import com.fiap.projects.apipassabola.repository.PlayerRepository;
import com.fiap.projects.apipassabola.repository.SpectatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {
    
    private final PlayerRepository playerRepository;
    private final OrganizationRepository organizationRepository;
    private final SpectatorRepository spectatorRepository;
    private final UserContextService userContextService;
    private final NotificationService notificationService;
    
    @Transactional
    public String followUser(FollowRequest request) {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        // Converter String userId para Long
        Long targetUserId = Long.parseLong(request.getTargetUserId());
        
        // Validar se não está tentando seguir a si mesmo (comparar userId global)
        Long currentUserId = getCurrentEntityUserId(currentUser.getUserId(), currentUser.getUserType());
        if (currentUserId.equals(targetUserId)) {
            throw new RuntimeException("You cannot follow yourself");
        }
        
        // Verificar se o usuário alvo existe
        if (!userExistsByUserId(targetUserId, request.getTargetUserType())) {
            throw new RuntimeException("Target user not found");
        }
        
        // Buscar entity IDs para verificar se já está seguindo
        Long followerEntityId = currentUser.getUserId();
        Long targetEntityId = getEntityIdByUserId(targetUserId, request.getTargetUserType());
        
        // Verificar se já está seguindo
        if (isFollowing(followerEntityId, currentUser.getUserType(), targetEntityId, request.getTargetUserType())) {
            throw new RuntimeException("You are already following this user");
        }
        
        // Executar o seguimento baseado nos tipos
        executeFollow(followerEntityId, currentUser.getUserType(), targetEntityId, request.getTargetUserType());
        
        // Enviar notificação em tempo real para o usuário que foi seguido
        sendFollowNotification(followerEntityId, currentUser.getUserType(), targetEntityId, request.getTargetUserType());
        
        return "Successfully followed user";
    }
    
    @Transactional
    public String unfollowUser(FollowRequest request) {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        // Converter String userId para Long
        Long targetUserId = Long.parseLong(request.getTargetUserId());
        
        // Buscar entity IDs
        Long followerEntityId = currentUser.getUserId();
        Long targetEntityId = getEntityIdByUserId(targetUserId, request.getTargetUserType());
        
        // Verificar se está seguindo
        if (!isFollowing(followerEntityId, currentUser.getUserType(), targetEntityId, request.getTargetUserType())) {
            throw new RuntimeException("You are not following this user");
        }
        
        // Executar o unfollow baseado nos tipos
        executeUnfollow(followerEntityId, currentUser.getUserType(), targetEntityId, request.getTargetUserType());
        
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
        List<FollowResponse> allFollowers = new ArrayList<>();
        
        switch (userType) {
            case PLAYER:
                // Buscar TODOS os tipos que seguem este player e combinar
                List<Player> playerFollowers = playerRepository.findFollowersByPlayerId(userId, Pageable.unpaged()).getContent();
                List<Organization> orgFollowers = playerRepository.findOrganizationFollowersByPlayerId(userId, Pageable.unpaged()).getContent();
                List<Spectator> spectatorFollowers = playerRepository.findSpectatorFollowersByPlayerId(userId, Pageable.unpaged()).getContent();
                
                allFollowers.addAll(playerFollowers.stream().map(this::convertPlayerToFollowResponse).collect(Collectors.toList()));
                allFollowers.addAll(orgFollowers.stream().map(this::convertOrganizationToFollowResponse).collect(Collectors.toList()));
                allFollowers.addAll(spectatorFollowers.stream().map(this::convertSpectatorToFollowResponse).collect(Collectors.toList()));
                break;
                
            case ORGANIZATION:
                // Buscar TODOS os tipos que seguem esta organization e combinar
                List<Player> playerFollowersOrg = organizationRepository.findPlayerFollowersByOrganizationId(userId, Pageable.unpaged()).getContent();
                List<Organization> orgFollowersOrg = organizationRepository.findOrganizationFollowersByOrganizationId(userId, Pageable.unpaged()).getContent();
                List<Spectator> spectatorFollowersOrg = organizationRepository.findSpectatorFollowersByOrganizationId(userId, Pageable.unpaged()).getContent();
                
                allFollowers.addAll(playerFollowersOrg.stream().map(this::convertPlayerToFollowResponse).collect(Collectors.toList()));
                allFollowers.addAll(orgFollowersOrg.stream().map(this::convertOrganizationToFollowResponse).collect(Collectors.toList()));
                allFollowers.addAll(spectatorFollowersOrg.stream().map(this::convertSpectatorToFollowResponse).collect(Collectors.toList()));
                break;
                
            case SPECTATOR:
                // Buscar TODOS os tipos que seguem este spectator e combinar
                List<Player> playerFollowersSpec = spectatorRepository.findPlayerFollowersBySpectatorId(userId, Pageable.unpaged()).getContent();
                List<Organization> orgFollowersSpec = spectatorRepository.findOrganizationFollowersBySpectatorId(userId, Pageable.unpaged()).getContent();
                List<Spectator> spectatorFollowersSpec = spectatorRepository.findFollowersBySpectatorId(userId, Pageable.unpaged()).getContent();
                
                allFollowers.addAll(playerFollowersSpec.stream().map(this::convertPlayerToFollowResponse).collect(Collectors.toList()));
                allFollowers.addAll(orgFollowersSpec.stream().map(this::convertOrganizationToFollowResponse).collect(Collectors.toList()));
                allFollowers.addAll(spectatorFollowersSpec.stream().map(this::convertSpectatorToFollowResponse).collect(Collectors.toList()));
                break;
                
            default:
                throw new RuntimeException("Invalid user type");
        }
        
        // Aplicar paginação manualmente
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allFollowers.size());
        List<FollowResponse> pageContent = allFollowers.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, allFollowers.size());
    }
    
    public Page<FollowResponse> getFollowing(Long userId, UserType userType, Pageable pageable) {
        List<FollowResponse> allFollowing = new ArrayList<>();
        
        switch (userType) {
            case PLAYER:
                // Buscar TODOS os tipos que este player segue e combinar
                List<Player> followingPlayers = playerRepository.findFollowingByPlayerId(userId, Pageable.unpaged()).getContent();
                List<Organization> followingOrgs = playerRepository.findFollowingOrganizationsByPlayerId(userId, Pageable.unpaged()).getContent();
                List<Spectator> followingSpectators = playerRepository.findFollowingSpectatorsByPlayerId(userId, Pageable.unpaged()).getContent();
                
                allFollowing.addAll(followingPlayers.stream().map(this::convertPlayerToFollowResponse).collect(Collectors.toList()));
                allFollowing.addAll(followingOrgs.stream().map(this::convertOrganizationToFollowResponse).collect(Collectors.toList()));
                allFollowing.addAll(followingSpectators.stream().map(this::convertSpectatorToFollowResponse).collect(Collectors.toList()));
                break;
                
            case ORGANIZATION:
                // Buscar TODOS os tipos que esta organization segue e combinar
                List<Player> followingPlayersOrg = organizationRepository.findFollowingPlayersByOrganizationId(userId, Pageable.unpaged()).getContent();
                List<Spectator> followingSpectatorsOrg = organizationRepository.findFollowingSpectatorsByOrganizationId(userId, Pageable.unpaged()).getContent();
                List<Organization> followingOrganizations = organizationRepository.findFollowingOrganizationsByOrganizationId(userId, Pageable.unpaged()).getContent();
                
                allFollowing.addAll(followingPlayersOrg.stream().map(this::convertPlayerToFollowResponse).collect(Collectors.toList()));
                allFollowing.addAll(followingSpectatorsOrg.stream().map(this::convertSpectatorToFollowResponse).collect(Collectors.toList()));
                allFollowing.addAll(followingOrganizations.stream().map(this::convertOrganizationToFollowResponse).collect(Collectors.toList()));
                break;
                
            case SPECTATOR:
                // Buscar TODOS os tipos que este spectator segue e combinar
                List<Player> followingPlayersSpec = spectatorRepository.findFollowingPlayersBySpectatorId(userId, Pageable.unpaged()).getContent();
                List<Organization> followingOrgsSpec = spectatorRepository.findFollowingOrganizationsBySpectatorId(userId, Pageable.unpaged()).getContent();
                List<Spectator> followingSpectatorsSpec = spectatorRepository.findFollowingBySpectatorId(userId, Pageable.unpaged()).getContent();
                
                allFollowing.addAll(followingPlayersSpec.stream().map(this::convertPlayerToFollowResponse).collect(Collectors.toList()));
                allFollowing.addAll(followingOrgsSpec.stream().map(this::convertOrganizationToFollowResponse).collect(Collectors.toList()));
                allFollowing.addAll(followingSpectatorsSpec.stream().map(this::convertSpectatorToFollowResponse).collect(Collectors.toList()));
                break;
                
            default:
                throw new RuntimeException("Invalid user type");
        }
        
        // Aplicar paginação manualmente
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allFollowing.size());
        List<FollowResponse> pageContent = allFollowing.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, allFollowing.size());
    }
    
    /**
     * Verifica se um usuário existe pelo userId global
     */
    public boolean userExistsByUserId(Long userId, UserType userType) {
        switch (userType) {
            case PLAYER:
                return playerRepository.existsByUserId(userId);
            case ORGANIZATION:
                return organizationRepository.existsByUserId(userId);
            case SPECTATOR:
                return spectatorRepository.existsByUserId(userId);
            default:
                return false;
        }
    }
    
    /**
     * Busca o ID da entidade (id) pelo userId global
     */
    public Long getEntityIdByUserId(Long userId, UserType userType) {
        switch (userType) {
            case PLAYER:
                return playerRepository.findByUserId(userId)
                    .map(Player::getId)
                    .orElseThrow(() -> new RuntimeException("Player not found with userId: " + userId));
            case ORGANIZATION:
                return organizationRepository.findByUserId(userId)
                    .map(Organization::getId)
                    .orElseThrow(() -> new RuntimeException("Organization not found with userId: " + userId));
            case SPECTATOR:
                return spectatorRepository.findByUserId(userId)
                    .map(Spectator::getId)
                    .orElseThrow(() -> new RuntimeException("Spectator not found with userId: " + userId));
            default:
                throw new RuntimeException("Invalid user type");
        }
    }
    
    /**
     * Busca o userId global do usuário atual
     */
    private Long getCurrentEntityUserId(Long entityId, UserType userType) {
        switch (userType) {
            case PLAYER:
                return playerRepository.findById(entityId)
                    .map(Player::getUserId)
                    .orElseThrow(() -> new RuntimeException("Player not found"));
            case ORGANIZATION:
                return organizationRepository.findById(entityId)
                    .map(Organization::getUserId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            case SPECTATOR:
                return spectatorRepository.findById(entityId)
                    .map(Spectator::getUserId)
                    .orElseThrow(() -> new RuntimeException("Spectator not found"));
            default:
                throw new RuntimeException("Invalid user type");
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
    
    private FollowResponse convertPlayerToFollowResponse(Player player) {
        FollowResponse response = new FollowResponse();
        response.setId(player.getId());  // Convert Long to String
        response.setUserId(String.valueOf(player.getUserId()));
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
        response.setId(organization.getId());  // Convert Long to String
        response.setUserId(String.valueOf(organization.getUserId()));
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
        response.setId(spectator.getId());  // Convert Long to String
        response.setUserId(String.valueOf(spectator.getUserId()));
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
    
    /**
     * Envia notificação de novo seguidor
     */
    private void sendFollowNotification(Long followerId, UserType followerType, Long targetId, UserType targetType) {
        try {
            // Buscar informações do seguidor
            String followerUsername = null;
            String followerName = null;
            Long followerUserId = null;
            
            switch (followerType) {
                case PLAYER:
                    Player followerPlayer = playerRepository.findById(followerId).orElse(null);
                    if (followerPlayer != null) {
                        followerUsername = followerPlayer.getRealUsername();
                        followerName = followerPlayer.getName();
                        followerUserId = followerPlayer.getUserId();
                    }
                    break;
                case ORGANIZATION:
                    Organization followerOrg = organizationRepository.findById(followerId).orElse(null);
                    if (followerOrg != null) {
                        followerUsername = followerOrg.getRealUsername();
                        followerName = followerOrg.getName();
                        followerUserId = followerOrg.getUserId();
                    }
                    break;
                case SPECTATOR:
                    Spectator followerSpectator = spectatorRepository.findById(followerId).orElse(null);
                    if (followerSpectator != null) {
                        followerUsername = followerSpectator.getRealUsername();
                        followerName = followerSpectator.getName();
                        followerUserId = followerSpectator.getUserId();
                    }
                    break;
            }
            
            // Buscar userId do alvo
            Long targetUserId = null;
            switch (targetType) {
                case PLAYER:
                    Player targetPlayer = playerRepository.findById(targetId).orElse(null);
                    if (targetPlayer != null) {
                        targetUserId = targetPlayer.getUserId();
                    }
                    break;
                case ORGANIZATION:
                    Organization targetOrg = organizationRepository.findById(targetId).orElse(null);
                    if (targetOrg != null) {
                        targetUserId = targetOrg.getUserId();
                    }
                    break;
                case SPECTATOR:
                    Spectator targetSpectator = spectatorRepository.findById(targetId).orElse(null);
                    if (targetSpectator != null) {
                        targetUserId = targetSpectator.getUserId();
                    }
                    break;
            }
            
            // Enviar notificação se todas as informações foram encontradas
            if (followerUsername != null && followerName != null && followerUserId != null && targetUserId != null) {
                notificationService.notifyNewFollower(
                        targetUserId,
                        targetType,
                        followerUserId,
                        followerType,
                        followerUsername,
                        followerName
                );
            }
        } catch (Exception e) {
            // Log mas não falha a operação de follow se a notificação falhar
            System.err.println("Erro ao enviar notificação de follow: " + e.getMessage());
        }
    }
}
