package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.auth.*;
import com.fiap.projects.apipassabola.entity.*;
import com.fiap.projects.apipassabola.exception.BusinessException;
import com.fiap.projects.apipassabola.exception.ResourceNotFoundException;
import com.fiap.projects.apipassabola.repository.*;
import com.fiap.projects.apipassabola.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final OrganizationRepository organizationRepository;
    private final SpectatorRepository spectatorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        User user = (User) authentication.getPrincipal();
        
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("userId", user.getId());
        
        String token = jwtUtil.generateToken(user, extraClaims);
        
        Long profileId = getProfileIdByUserAndRole(user);
        
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getRole(), profileId);
    }
    
    public AuthResponse registerPlayer(PlayerRegistrationRequest request) {
        validateUniqueCredentials(request.getUsername(), request.getEmail());
        
        // Create User
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.PLAYER);
        user = userRepository.save(user);
        
        // Create Player
        Player player = new Player();
        player.setUser(user);
        player.setFirstName(request.getFirstName());
        player.setLastName(request.getLastName());
        player.setBio(request.getBio());
        player.setBirthDate(request.getBirthDate());
        player.setPosition(request.getPosition());
        player.setProfilePhotoUrl(request.getProfilePhotoUrl());
        player.setJerseyNumber(request.getJerseyNumber());
        
        if (request.getOrganizationId() != null) {
            organizationRepository.findById(request.getOrganizationId())
                .ifPresent(player::setOrganization);
        }
        
        player = playerRepository.save(player);
        
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("userId", user.getId());
        extraClaims.put("playerId", player.getId());
        
        String token = jwtUtil.generateToken(user, extraClaims);
        
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getRole(), player.getId());
    }
    
    public AuthResponse registerOrganization(OrganizationRegistrationRequest request) {
        validateUniqueCredentials(request.getUsername(), request.getEmail());
        
        // Create User
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.ORGANIZATION);
        user = userRepository.save(user);
        
        // Create Organization
        Organization organization = new Organization();
        organization.setUser(user);
        organization.setName(request.getName());
        organization.setDescription(request.getDescription());
        organization.setCity(request.getCity());
        organization.setState(request.getState());
        organization.setLogoUrl(request.getLogoUrl());
        organization.setPrimaryColors(request.getPrimaryColors());
        organization.setFoundedYear(request.getFoundedYear());
        organization.setWebsiteUrl(request.getWebsiteUrl());
        organization.setContactEmail(request.getContactEmail());
        organization.setContactPhone(request.getContactPhone());
        
        organization = organizationRepository.save(organization);
        
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("userId", user.getId());
        extraClaims.put("organizationId", organization.getId());
        
        String token = jwtUtil.generateToken(user, extraClaims);
        
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getRole(), organization.getId());
    }
    
    public AuthResponse registerSpectator(SpectatorRegistrationRequest request) {
        validateUniqueCredentials(request.getUsername(), request.getEmail());
        
        // Create User
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.SPECTATOR);
        user = userRepository.save(user);
        
        // Create Spectator
        Spectator spectator = new Spectator();
        spectator.setUser(user);
        spectator.setFirstName(request.getFirstName());
        spectator.setLastName(request.getLastName());
        spectator.setBio(request.getBio());
        spectator.setBirthDate(request.getBirthDate());
        spectator.setProfilePhotoUrl(request.getProfilePhotoUrl());
        if (request.getFavoriteTeamId() != null) {
            Organization favoriteTeam = organizationRepository.findById(request.getFavoriteTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getFavoriteTeamId()));
            spectator.setFavoriteTeam(favoriteTeam);
        }
        
        spectator = spectatorRepository.save(spectator);
        
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("userId", user.getId());
        extraClaims.put("spectatorId", spectator.getId());
        
        String token = jwtUtil.generateToken(user, extraClaims);
        
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getRole(), spectator.getId());
    }
    
    private void validateUniqueCredentials(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("Username '" + username + "' already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email '" + email + "' already exists");
        }
    }
    
    private Long getProfileIdByUserAndRole(User user) {
        return switch (user.getRole()) {
            case PLAYER -> playerRepository.findByUserId(user.getId())
                .map(Player::getId)
                .orElse(null);
            case ORGANIZATION -> organizationRepository.findByUserId(user.getId())
                .map(Organization::getId)
                .orElse(null);
            case SPECTATOR -> spectatorRepository.findByUserId(user.getId())
                .map(Spectator::getId)
                .orElse(null);
        };
    }
}
