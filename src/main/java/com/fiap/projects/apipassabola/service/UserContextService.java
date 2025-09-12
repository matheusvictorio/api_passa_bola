package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.entity.User;
import com.fiap.projects.apipassabola.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserContextService {
    
    private final UserRepository userRepository;
    
    /**
     * Gets the currently authenticated user from the security context
     * @return Current authenticated user
     * @throws RuntimeException if no user is authenticated or user not found
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }
        
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
    
    /**
     * Gets the ID of the currently authenticated user
     * @return Current user's ID
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
