package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.auth.*;
import com.fiap.projects.apipassabola.entity.*;
import com.fiap.projects.apipassabola.exception.BusinessException;
import com.fiap.projects.apipassabola.exception.ResourceNotFoundException;
import com.fiap.projects.apipassabola.repository.*;
import com.fiap.projects.apipassabola.security.JwtUtil;
import com.fiap.projects.apipassabola.util.CnpjValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    
    private final PlayerRepository playerRepository;
    private final OrganizationRepository organizationRepository;
    private final SpectatorRepository spectatorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // Find the actual entity based on email
        String email = userDetails.getUsername(); // Spring Security uses getUsername() method, but we store email there
        
        // Try to find in each repository
        var playerOpt = playerRepository.findByEmail(email);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("role", player.getUserType().name());
            extraClaims.put("userId", player.getId());
            
            String token = jwtUtil.generateToken(player, extraClaims);
            return new AuthResponse(token, player.getId(), player.getRealUsername(), player.getEmail(), 
                    User.Role.valueOf(player.getUserType().name()), player.getId());
        }
        
        var organizationOpt = organizationRepository.findByEmail(email);
        if (organizationOpt.isPresent()) {
            Organization organization = organizationOpt.get();
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("role", organization.getUserType().name());
            extraClaims.put("userId", organization.getId());
            
            String token = jwtUtil.generateToken(organization, extraClaims);
            return new AuthResponse(token, organization.getId(), organization.getRealUsername(), organization.getEmail(), 
                    User.Role.valueOf(organization.getUserType().name()), organization.getId());
        }
        
        var spectatorOpt = spectatorRepository.findByEmail(email);
        if (spectatorOpt.isPresent()) {
            Spectator spectator = spectatorOpt.get();
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("role", spectator.getUserType().name());
            extraClaims.put("userId", spectator.getId());
            
            String token = jwtUtil.generateToken(spectator, extraClaims);
            return new AuthResponse(token, spectator.getId(), spectator.getRealUsername(), spectator.getEmail(), 
                    User.Role.valueOf(spectator.getUserType().name()), spectator.getId());
        }
        
        throw new BusinessException("User not found");
    }
    
    public AuthResponse registerPlayer(PlayerRegistrationRequest request) {
        validateUniqueCredentials(request.getUsername(), request.getEmail());
        
        // Create Player with flattened structure
        Player player = new Player();
        player.setUserType(UserType.PLAYER);
        player.setUsername(request.getUsername());
        player.setName(request.getName());
        player.setEmail(request.getEmail());
        player.setPassword(passwordEncoder.encode(request.getPassword()));
        player.setBio(request.getBio());
        player.setBirthDate(request.getBirthDate());
        player.setProfilePhotoUrl(request.getProfilePhotoUrl());
        player.setBannerUrl(request.getBannerUrl());
        player.setPhone(request.getPhone());
        player.setPastOrganization(request.getPastOrganization());
        player.setGamesPlayed(request.getGamesPlayed() != null ? request.getGamesPlayed() : 0);
        
        if (request.getOrganizationId() != null) {
            organizationRepository.findById(request.getOrganizationId())
                .ifPresent(player::setOrganization);
        }
        
        player = playerRepository.save(player);
        
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", player.getUserType().name());
        extraClaims.put("userId", player.getId());
        extraClaims.put("playerId", player.getId());
        
        String token = jwtUtil.generateToken(player, extraClaims);
        
        return new AuthResponse(token, player.getId(), player.getRealUsername(), player.getEmail(), 
                User.Role.valueOf(player.getUserType().name()), player.getId());
    }
    
    public AuthResponse registerOrganization(OrganizationRegistrationRequest request) {
        validateUniqueCredentials(request.getUsername(), request.getEmail());
        
        // Validate CNPJ uniqueness
        String normalizedCnpj = CnpjValidator.unformat(request.getCnpj());
        if (organizationRepository.findByCnpj(normalizedCnpj).isPresent()) {
            throw new BusinessException("CNPJ already exists for another organization");
        }
        
        // Create Organization with flattened structure
        Organization organization = new Organization();
        organization.setUserType(UserType.ORGANIZATION);
        organization.setUsername(request.getUsername());
        organization.setName(request.getName());
        organization.setEmail(request.getEmail());
        organization.setPassword(passwordEncoder.encode(request.getPassword()));
        organization.setCnpj(normalizedCnpj);
        organization.setBio(request.getBio());
        organization.setProfilePhotoUrl(request.getProfilePhotoUrl());
        organization.setBannerUrl(request.getBannerUrl());
        organization.setPhone(request.getPhone());
        organization.setCity(request.getCity());
        organization.setState(request.getState());
        // Set default values for fields not in registration request
        organization.setGamesPlayed(0);
        
        organization = organizationRepository.save(organization);
        
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", organization.getUserType().name());
        extraClaims.put("userId", organization.getId());
        extraClaims.put("organizationId", organization.getId());
        
        String token = jwtUtil.generateToken(organization, extraClaims);
        
        return new AuthResponse(token, organization.getId(), organization.getUsername(), organization.getEmail(), 
                User.Role.valueOf(organization.getUserType().name()), organization.getId());
    }
    
    public AuthResponse registerSpectator(SpectatorRegistrationRequest request) {
        validateUniqueCredentials(request.getUsername(), request.getEmail());
        
        // Create Spectator with flattened structure
        Spectator spectator = new Spectator();
        spectator.setUserType(UserType.SPECTATOR);
        spectator.setUsername(request.getUsername());
        spectator.setName(request.getFirstName() + " " + request.getLastName());
        spectator.setEmail(request.getEmail());
        spectator.setPassword(passwordEncoder.encode(request.getPassword()));
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
        extraClaims.put("role", spectator.getUserType().name());
        extraClaims.put("userId", spectator.getId());
        extraClaims.put("spectatorId", spectator.getId());
        
        String token = jwtUtil.generateToken(spectator, extraClaims);
        
        return new AuthResponse(token, spectator.getId(), spectator.getRealUsername(), spectator.getEmail(), 
                User.Role.SPECTATOR, spectator.getId());
    }
    
    private void validateUniqueCredentials(String username, String email) {
        // Check username uniqueness across all entity types
        if (playerRepository.findByUsername(username).isPresent() ||
            organizationRepository.findByUsername(username).isPresent() ||
            spectatorRepository.findByUsername(username).isPresent()) {
            throw new BusinessException("Username '" + username + "' already exists");
        }
        
        // Check email uniqueness across all entity types
        if (playerRepository.findByEmail(email).isPresent() ||
            organizationRepository.findByEmail(email).isPresent() ||
            spectatorRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("Email '" + email + "' already exists");
        }
    }
}
