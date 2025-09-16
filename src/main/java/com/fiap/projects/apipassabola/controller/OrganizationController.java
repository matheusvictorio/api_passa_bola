package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.request.OrganizationRequest;
import com.fiap.projects.apipassabola.dto.response.OrganizationResponse;
import com.fiap.projects.apipassabola.dto.response.PlayerResponse;
import com.fiap.projects.apipassabola.dto.response.SpectatorResponse;
import com.fiap.projects.apipassabola.entity.Organization;
import com.fiap.projects.apipassabola.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrganizationController {
    
    private final OrganizationService organizationService;
    
    @GetMapping
    public ResponseEntity<Page<OrganizationResponse>> getAllOrganizations(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrganizationResponse> organizations = organizationService.findAll(pageable);
        return ResponseEntity.ok(organizations);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getOrganizationById(@PathVariable Long id) {
        OrganizationResponse organization = organizationService.findById(id);
        return ResponseEntity.ok(organization);
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<OrganizationResponse> getOrganizationByUsername(@PathVariable String username) {
        OrganizationResponse organization = organizationService.findByUsername(username);
        return ResponseEntity.ok(organization);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<OrganizationResponse>> searchOrganizationsByName(
            @RequestParam String name,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrganizationResponse> organizations = organizationService.findByName(name, pageable);
        return ResponseEntity.ok(organizations);
    }
    
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<OrganizationResponse> updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationRequest request) {
        OrganizationResponse updatedOrganization = organizationService.update(id, request);
        return ResponseEntity.ok(updatedOrganization);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<Void> deleteOrganization(@PathVariable Long id) {
        organizationService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    // ========== CROSS-TYPE FOLLOWING ENDPOINTS FOR ORGANIZATION ==========
    
    // Organization following Players
    @PostMapping("/players/{playerId}/follow")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<Void> followPlayer(
            @PathVariable Long playerId,
            Authentication authentication) {
        Organization organization = (Organization) authentication.getPrincipal();
        organizationService.followPlayer(organization.getId(), playerId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/players/{playerId}/follow")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<Void> unfollowPlayer(
            @PathVariable Long playerId,
            Authentication authentication) {
        Organization organization = (Organization) authentication.getPrincipal();
        organizationService.unfollowPlayer(organization.getId(), playerId);
        return ResponseEntity.ok().build();
    }
    
    // Organization following Spectators
    @PostMapping("/spectators/{spectatorId}/follow")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<Void> followSpectator(
            @PathVariable Long spectatorId,
            Authentication authentication) {
        Organization organization = (Organization) authentication.getPrincipal();
        organizationService.followSpectator(organization.getId(), spectatorId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/spectators/{spectatorId}/follow")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<Void> unfollowSpectator(
            @PathVariable Long spectatorId,
            Authentication authentication) {
        Organization organization = (Organization) authentication.getPrincipal();
        organizationService.unfollowSpectator(organization.getId(), spectatorId);
        return ResponseEntity.ok().build();
    }
    
    // Organization following Organizations
    @PostMapping("/{organizationId}/follow")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<Void> followOrganization(
            @PathVariable Long organizationId,
            Authentication authentication) {
        Organization organization = (Organization) authentication.getPrincipal();
        organizationService.followOrganization(organization.getId(), organizationId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{organizationId}/follow")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<Void> unfollowOrganization(
            @PathVariable Long organizationId,
            Authentication authentication) {
        Organization organization = (Organization) authentication.getPrincipal();
        organizationService.unfollowOrganization(organization.getId(), organizationId);
        return ResponseEntity.ok().build();
    }
    
    // Get following lists for Organization (cross-type)
    @GetMapping("/{id}/following-players")
    public ResponseEntity<Page<PlayerResponse>> getOrganizationFollowingPlayers(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PlayerResponse> followingPlayers = organizationService.getFollowingPlayers(id, pageable);
        return ResponseEntity.ok(followingPlayers);
    }
    
    @GetMapping("/{id}/following-spectators")
    public ResponseEntity<Page<SpectatorResponse>> getOrganizationFollowingSpectators(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SpectatorResponse> followingSpectators = organizationService.getFollowingSpectators(id, pageable);
        return ResponseEntity.ok(followingSpectators);
    }
    
    @GetMapping("/{id}/following-organizations")
    public ResponseEntity<Page<OrganizationResponse>> getOrganizationFollowingOrganizations(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrganizationResponse> followingOrganizations = organizationService.getFollowing(id, pageable);
        return ResponseEntity.ok(followingOrganizations);
    }
    
    // Check following status for Organization (cross-type)
    @GetMapping("/players/{playerId}/is-following")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<Boolean> isFollowingPlayer(
            @PathVariable Long playerId,
            Authentication authentication) {
        Organization organization = (Organization) authentication.getPrincipal();
        boolean isFollowing = organizationService.isFollowingPlayer(organization.getId(), playerId);
        return ResponseEntity.ok(isFollowing);
    }
    
    @GetMapping("/spectators/{spectatorId}/is-following")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<Boolean> isFollowingSpectator(
            @PathVariable Long spectatorId,
            Authentication authentication) {
        Organization organization = (Organization) authentication.getPrincipal();
        boolean isFollowing = organizationService.isFollowingSpectator(organization.getId(), spectatorId);
        return ResponseEntity.ok(isFollowing);
    }
    
    @GetMapping("/{organizationId}/is-following")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<Boolean> isFollowingOrganization(
            @PathVariable Long organizationId,
            Authentication authentication) {
        Organization organization = (Organization) authentication.getPrincipal();
        boolean isFollowing = organizationService.isFollowingOrganization(organization.getId(), organizationId);
        return ResponseEntity.ok(isFollowing);
    }
    
    // Get followers lists for Organization (cross-type)
    @GetMapping("/{id}/player-followers")
    public ResponseEntity<Page<PlayerResponse>> getOrganizationPlayerFollowers(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PlayerResponse> playerFollowers = organizationService.getPlayerFollowers(id, pageable);
        return ResponseEntity.ok(playerFollowers);
    }
    
    @GetMapping("/{id}/spectator-followers")
    public ResponseEntity<Page<SpectatorResponse>> getOrganizationSpectatorFollowers(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SpectatorResponse> spectatorFollowers = organizationService.getSpectatorFollowers(id, pageable);
        return ResponseEntity.ok(spectatorFollowers);
    }
    
    @GetMapping("/{id}/organization-followers")
    public ResponseEntity<Page<OrganizationResponse>> getOrganizationFollowers(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrganizationResponse> organizationFollowers = organizationService.getFollowers(id, pageable);
        return ResponseEntity.ok(organizationFollowers);
    }
    
    // Personal following lists for authenticated Organization
    @GetMapping("/my-following-players")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<Page<PlayerResponse>> getMyFollowingPlayers(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        Organization organization = (Organization) authentication.getPrincipal();
        Page<PlayerResponse> followingPlayers = organizationService.getFollowingPlayers(organization.getId(), pageable);
        return ResponseEntity.ok(followingPlayers);
    }
    
    @GetMapping("/my-following-spectators")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<Page<SpectatorResponse>> getMyFollowingSpectators(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        Organization organization = (Organization) authentication.getPrincipal();
        Page<SpectatorResponse> followingSpectators = organizationService.getFollowingSpectators(organization.getId(), pageable);
        return ResponseEntity.ok(followingSpectators);
    }
    
    @GetMapping("/my-following-organizations")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<Page<OrganizationResponse>> getMyFollowingOrganizations(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        Organization organization = (Organization) authentication.getPrincipal();
        Page<OrganizationResponse> followingOrganizations = organizationService.getFollowing(organization.getId(), pageable);
        return ResponseEntity.ok(followingOrganizations);
    }
}
