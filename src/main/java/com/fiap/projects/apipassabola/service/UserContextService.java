package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.entity.*;
import com.fiap.projects.apipassabola.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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
     * @return UserIdAndType object with userId and userType
     */
    public UserIdAndType getCurrentUserIdAndType() {
        String email = getCurrentUserDetails().getUsername(); // getUsername() returns email in our system
        
        Player player = playerRepository.findByEmail(email).orElse(null);
        if (player != null) {
            return new UserIdAndType(player.getId(), UserType.PLAYER);
        }
        
        Organization organization = organizationRepository.findByEmail(email).orElse(null);
        if (organization != null) {
            return new UserIdAndType(organization.getId(), UserType.ORGANIZATION);
        }
        
        Spectator spectator = spectatorRepository.findByEmail(email).orElse(null);
        if (spectator != null) {
            return new UserIdAndType(spectator.getId(), UserType.SPECTATOR);
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
}
