package com.fiap.projects.apipassabola.security;

import com.fiap.projects.apipassabola.repository.PlayerRepository;
import com.fiap.projects.apipassabola.repository.OrganizationRepository;
import com.fiap.projects.apipassabola.repository.SpectatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final PlayerRepository playerRepository;
    private final OrganizationRepository organizationRepository;
    private final SpectatorRepository spectatorRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Try to find user by email in Player repository
        var playerOpt = playerRepository.findByEmail(email);
        if (playerOpt.isPresent()) {
            return playerOpt.get();
        }
        
        // Try to find user by email in Organization repository
        var organizationOpt = organizationRepository.findByEmail(email);
        if (organizationOpt.isPresent()) {
            return organizationOpt.get();
        }
        
        // Try to find user by email in Spectator repository
        var spectatorOpt = spectatorRepository.findByEmail(email);
        if (spectatorOpt.isPresent()) {
            return spectatorOpt.get();
        }
        
        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
