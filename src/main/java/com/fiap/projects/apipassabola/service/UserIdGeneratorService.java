package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.repository.OrganizationRepository;
import com.fiap.projects.apipassabola.repository.PlayerRepository;
import com.fiap.projects.apipassabola.repository.SpectatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Service to generate unique global user IDs across all user types.
 * This ensures that every user (Player, Organization, Spectator) has a unique userId.
 */
@Service
@RequiredArgsConstructor
public class UserIdGeneratorService {
    
    private final PlayerRepository playerRepository;
    private final OrganizationRepository organizationRepository;
    private final SpectatorRepository spectatorRepository;
    private final Random random = new Random();
    
    /**
     * Generates a unique user ID that doesn't exist in any user type.
     * Uses a random long value and checks against all repositories.
     * 
     * @return A unique user ID
     */
    public Long generateUniqueUserId() {
        Long userId;
        int attempts = 0;
        int maxAttempts = 100;
        
        do {
            // Generate a random positive long (avoiding negative values)
            userId = Math.abs(random.nextLong());
            
            // Ensure it's not zero
            if (userId == 0L) {
                userId = 1L;
            }
            
            attempts++;
            
            // Safety check to avoid infinite loop
            if (attempts >= maxAttempts) {
                throw new RuntimeException("Failed to generate unique user ID after " + maxAttempts + " attempts");
            }
            
        } while (userIdExists(userId));
        
        return userId;
    }
    
    /**
     * Checks if a user ID already exists in any user type.
     * 
     * @param userId The user ID to check
     * @return true if the ID exists, false otherwise
     */
    private boolean userIdExists(Long userId) {
        return playerRepository.existsByUserId(userId) ||
               organizationRepository.existsByUserId(userId) ||
               spectatorRepository.existsByUserId(userId);
    }
}
