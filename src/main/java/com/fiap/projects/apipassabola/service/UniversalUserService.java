package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.entity.*;
import com.fiap.projects.apipassabola.repository.OrganizationRepository;
import com.fiap.projects.apipassabola.repository.PlayerRepository;
import com.fiap.projects.apipassabola.repository.SpectatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Universal service to find users across all types (PLAYER, ORGANIZATION, SPECTATOR)
 * Uses global userId (snowflake) for cross-type operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UniversalUserService {

    private final PlayerRepository playerRepository;
    private final OrganizationRepository organizationRepository;
    private final SpectatorRepository spectatorRepository;

    /**
     * User info wrapper
     */
    public static class UserInfo {
        public final Long userId;
        public final String email;
        public final String username;
        public final String name;
        public final UserType userType;

        public UserInfo(Long userId, String email, String username, String name, UserType userType) {
            this.userId = userId;
            this.email = email;
            this.username = username;
            this.name = name;
            this.userType = userType;
        }
    }

    /**
     * Get current authenticated user info from SecurityContext
     */
    public UserInfo getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String email = authentication.getName();
        return findByEmail(email);
    }

    /**
     * Get user info from Principal (for WebSocket contexts)
     */
    public UserInfo getUserFromPrincipal(java.security.Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Principal is null - user not authenticated");
        }
        
        String email = principal.getName();
        log.debug("Getting user from principal: email={}", email);
        return findByEmail(email);
    }

    /**
     * Find user by email across all types
     */
    public UserInfo findByEmail(String email) {
        // Try Player
        var playerOpt = playerRepository.findByEmail(email);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            log.debug("Found player: id={}, userId={}, email={}", player.getId(), player.getUserId(), player.getEmail());
            return new UserInfo(
                    player.getUserId(),
                    player.getEmail(),
                    player.getRealUsername(),
                    player.getName(),
                    player.getUserType()
            );
        }

        // Try Organization
        var orgOpt = organizationRepository.findByEmail(email);
        if (orgOpt.isPresent()) {
            Organization org = orgOpt.get();
            return new UserInfo(
                    org.getUserId(),
                    org.getEmail(),
                    org.getRealUsername(),
                    org.getName(),
                    org.getUserType()
            );
        }

        // Try Spectator
        var spectatorOpt = spectatorRepository.findByEmail(email);
        if (spectatorOpt.isPresent()) {
            Spectator spectator = spectatorOpt.get();
            return new UserInfo(
                    spectator.getUserId(),
                    spectator.getEmail(),
                    spectator.getRealUsername(),
                    spectator.getName(),
                    spectator.getUserType()
            );
        }

        throw new RuntimeException("User not found: " + email);
    }

    /**
     * Find user by global userId across all types
     */
    public UserInfo findByUserId(Long userId) {
        // Try Player
        var playerOpt = playerRepository.findByUserId(userId);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            return new UserInfo(
                    player.getUserId(),
                    player.getEmail(),
                    player.getRealUsername(),
                    player.getName(),
                    player.getUserType()
            );
        }

        // Try Organization
        var orgOpt = organizationRepository.findByUserId(userId);
        if (orgOpt.isPresent()) {
            Organization org = orgOpt.get();
            return new UserInfo(
                    org.getUserId(),
                    org.getEmail(),
                    org.getRealUsername(),
                    org.getName(),
                    org.getUserType()
            );
        }

        // Try Spectator
        var spectatorOpt = spectatorRepository.findByUserId(userId);
        if (spectatorOpt.isPresent()) {
            Spectator spectator = spectatorOpt.get();
            return new UserInfo(
                    spectator.getUserId(),
                    spectator.getEmail(),
                    spectator.getRealUsername(),
                    spectator.getName(),
                    spectator.getUserType()
            );
        }

        throw new RuntimeException("User not found with userId: " + userId);
    }
}
