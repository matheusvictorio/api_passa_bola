package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.entity.*;
import com.fiap.projects.apipassabola.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserContextService {
    
    private final PlayerRepository playerRepository;
    private final OrganizationRepository organizationRepository;
    private final SpectatorRepository spectatorRepository;
    
    /**
     * Gets the currently authenticated user details from the security context
     * @return Current authenticated user details
     * @throws RuntimeException if no user is authenticated
     */
    public UserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }
        
        return (UserDetails) authentication.getPrincipal();
    }
    
    /**
     * Gets the currently authenticated player
     * @return Current authenticated player or null if not a player
     */
    public Player getCurrentPlayer() {
        String email = getCurrentUserDetails().getUsername(); // getUsername() returns email in our system
        return playerRepository.findByEmail(email).orElse(null);
    }
    
    /**
     * Gets the currently authenticated organization
     * @return Current authenticated organization or null if not an organization
     */
    public Organization getCurrentOrganization() {
        String email = getCurrentUserDetails().getUsername(); // getUsername() returns email in our system
        return organizationRepository.findByEmail(email).orElse(null);
    }
    
    /**
     * Gets the currently authenticated spectator
     * @return Current authenticated spectator or null if not a spectator
     */
    public Spectator getCurrentSpectator() {
        String email = getCurrentUserDetails().getUsername(); // getUsername() returns email in our system
        return spectatorRepository.findByEmail(email).orElse(null);
    }
    
    /**
     * Gets the ID and type of the currently authenticated user
     * IMPORTANT: Returns the ENTITY ID (not userId global) for internal operations
     * @return UserIdAndType object with entity ID and userType
     */
    public UserIdAndType getCurrentUserIdAndType() {
        String email = getCurrentUserDetails().getUsername(); // getUsername() returns email in our system
        
        Player player = playerRepository.findByEmail(email).orElse(null);
        if (player != null) {
            return new UserIdAndType(player.getId(), UserType.PLAYER);  // Entity ID for internal operations
        }
        
        Organization organization = organizationRepository.findByEmail(email).orElse(null);
        if (organization != null) {
            return new UserIdAndType(organization.getId(), UserType.ORGANIZATION);  // Entity ID for internal operations
        }
        
        Spectator spectator = spectatorRepository.findByEmail(email).orElse(null);
        if (spectator != null) {
            return new UserIdAndType(spectator.getId(), UserType.SPECTATOR);  // Entity ID for internal operations
        }
        
        throw new RuntimeException("User not found: " + email);
    }
    
    /**
     * Gets the GLOBAL USER ID and type of the currently authenticated user
     * IMPORTANT: Returns the GLOBAL userId (used for notifications, cross-type operations)
     * @return UserIdAndType object with global userId and userType
     */
    public UserIdAndType getCurrentGlobalUserIdAndType() {
        String email = getCurrentUserDetails().getUsername(); // getUsername() returns email in our system
        
        Player player = playerRepository.findByEmail(email).orElse(null);
        if (player != null) {
            log.debug("getCurrentGlobalUserIdAndType - Player encontrado: email={}, userId={}, entityId={}", 
                    email, player.getUserId(), player.getId());
            return new UserIdAndType(player.getUserId(), UserType.PLAYER);  // Global userId
        }
        
        Organization organization = organizationRepository.findByEmail(email).orElse(null);
        if (organization != null) {
            log.debug("getCurrentGlobalUserIdAndType - Organization encontrada: email={}, userId={}, entityId={}", 
                    email, organization.getUserId(), organization.getId());
            return new UserIdAndType(organization.getUserId(), UserType.ORGANIZATION);  // Global userId
        }
        
        Spectator spectator = spectatorRepository.findByEmail(email).orElse(null);
        if (spectator != null) {
            log.debug("getCurrentGlobalUserIdAndType - Spectator encontrado: email={}, userId={}, entityId={}", 
                    email, spectator.getUserId(), spectator.getId());
            return new UserIdAndType(spectator.getUserId(), UserType.SPECTATOR);  // Global userId
        }
        
        throw new RuntimeException("User not found: " + email);
    }
    
    /**
     * Inner class to hold user ID and type information
     */
    public static class UserIdAndType {
        private final Long userId;
        private final UserType userType;
        
        public UserIdAndType(Long userId, UserType userType) {
            this.userId = userId;
            this.userType = userType;
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public UserType getUserType() {
            return userType;
        }
    }
    
    /**
     * Gets the ID of the currently authenticated user
     * @return Current user's ID
     */
    public Long getCurrentUserId() {
        UserIdAndType userInfo = getCurrentUserIdAndType();
        return userInfo.getUserId();
    }
    
    /**
     * Gets the type of the currently authenticated user
     * @return Current user's type
     */
    public UserType getCurrentUserType() {
        UserIdAndType userInfo = getCurrentUserIdAndType();
        return userInfo.getUserType();
    }
    
    /**
     * Gets the username of the currently authenticated user
     * Note: This returns email for Spring Security compatibility
     * @return Current user's email (used as username for authentication)
     */
    public String getCurrentUsername() {
        return getCurrentUserDetails().getUsername();
    }
    
    /**
     * Gets the real username (not email) of the currently authenticated user
     * @return Current user's actual username field
     */
    public String getCurrentRealUsername() {
        UserIdAndType userInfo = getCurrentUserIdAndType();
        Long userId = userInfo.getUserId();
        UserType userType = userInfo.getUserType();
        
        switch (userType) {
            case PLAYER:
                return playerRepository.findById(userId)
                        .map(Player::getRealUsername)
                        .orElse("Unknown Player");
            case ORGANIZATION:
                return organizationRepository.findById(userId)
                        .map(Organization::getRealUsername)
                        .orElse("Unknown Organization");
            case SPECTATOR:
                return spectatorRepository.findById(userId)
                        .map(Spectator::getRealUsername)
                        .orElse("Unknown Spectator");
            default:
                return "Unknown User";
        }
    }
    
    /**
     * Gets the name of the currently authenticated user
     * @return Current user's name field
     */
    public String getCurrentUserName() {
        UserIdAndType userInfo = getCurrentUserIdAndType();
        Long userId = userInfo.getUserId();
        UserType userType = userInfo.getUserType();
        
        switch (userType) {
            case PLAYER:
                return playerRepository.findById(userId)
                        .map(Player::getName)
                        .orElse("Unknown Player");
            case ORGANIZATION:
                return organizationRepository.findById(userId)
                        .map(Organization::getName)
                        .orElse("Unknown Organization");
            case SPECTATOR:
                return spectatorRepository.findById(userId)
                        .map(Spectator::getName)
                        .orElse("Unknown Spectator");
            default:
                return "Unknown User";
        }
    }
}
