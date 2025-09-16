package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.request.SpectatorRequest;
import com.fiap.projects.apipassabola.dto.response.OrganizationResponse;
import com.fiap.projects.apipassabola.dto.response.PlayerResponse;
import com.fiap.projects.apipassabola.dto.response.SpectatorResponse;
import com.fiap.projects.apipassabola.service.SpectatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spectators")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SpectatorController {
    
    private final SpectatorService spectatorService;
    
    @GetMapping
    public ResponseEntity<Page<SpectatorResponse>> getAllSpectators(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SpectatorResponse> spectators = spectatorService.findAll(pageable);
        return ResponseEntity.ok(spectators);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SpectatorResponse> getSpectatorById(@PathVariable Long id) {
        SpectatorResponse spectator = spectatorService.findById(id);
        return ResponseEntity.ok(spectator);
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<SpectatorResponse> getSpectatorByUsername(@PathVariable String username) {
        SpectatorResponse spectator = spectatorService.findByUsername(username);
        return ResponseEntity.ok(spectator);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<SpectatorResponse>> searchSpectatorsByName(
            @RequestParam String name,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SpectatorResponse> spectators = spectatorService.findByName(name, pageable);
        return ResponseEntity.ok(spectators);
    }
    
    @GetMapping("/favorite-team/{favoriteTeamId}")
    public ResponseEntity<Page<SpectatorResponse>> getSpectatorsByFavoriteTeam(
            @PathVariable Long favoriteTeamId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SpectatorResponse> spectators = spectatorService.findByFavoriteTeam(favoriteTeamId, pageable);
        return ResponseEntity.ok(spectators);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<SpectatorResponse> updateSpectator(
            @PathVariable Long id,
            @Valid @RequestBody SpectatorRequest request) {
        SpectatorResponse updatedSpectator = spectatorService.update(id, request);
        return ResponseEntity.ok(updatedSpectator);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<Void> deleteSpectator(@PathVariable Long id) {
        spectatorService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    // Following/Followers endpoints
    
    @PostMapping("/{spectatorId}/follow")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<Void> followSpectator(@PathVariable Long spectatorId) {
        spectatorService.followSpectator(spectatorId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{spectatorId}/follow")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<Void> unfollowSpectator(@PathVariable Long spectatorId) {
        spectatorService.unfollowSpectator(spectatorId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{spectatorId}/followers")
    public ResponseEntity<Page<SpectatorResponse>> getFollowers(
            @PathVariable Long spectatorId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SpectatorResponse> followers = spectatorService.getFollowers(spectatorId, pageable);
        return ResponseEntity.ok(followers);
    }
    
    @GetMapping("/{spectatorId}/following")
    public ResponseEntity<Page<SpectatorResponse>> getFollowing(
            @PathVariable Long spectatorId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SpectatorResponse> following = spectatorService.getFollowing(spectatorId, pageable);
        return ResponseEntity.ok(following);
    }
    
    @GetMapping("/{spectatorId}/is-following")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<Boolean> isFollowing(@PathVariable Long spectatorId) {
        boolean isFollowing = spectatorService.isFollowing(spectatorId);
        return ResponseEntity.ok(isFollowing);
    }
    
    @GetMapping("/my-followers")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<Page<SpectatorResponse>> getMyFollowers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SpectatorResponse> followers = spectatorService.getMyFollowers(pageable);
        return ResponseEntity.ok(followers);
    }
    
    @GetMapping("/my-following")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<Page<SpectatorResponse>> getMyFollowing(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SpectatorResponse> following = spectatorService.getMyFollowing(pageable);
        return ResponseEntity.ok(following);
    }
    
    // Cross-type following endpoints
    
    @PostMapping("/players/{playerId}/follow")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<Void> followPlayer(@PathVariable Long playerId) {
        spectatorService.followPlayer(playerId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/players/{playerId}/follow")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<Void> unfollowPlayer(@PathVariable Long playerId) {
        spectatorService.unfollowPlayer(playerId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/organizations/{organizationId}/follow")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<Void> followOrganization(@PathVariable Long organizationId) {
        spectatorService.followOrganization(organizationId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/organizations/{organizationId}/follow")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<Void> unfollowOrganization(@PathVariable Long organizationId) {
        spectatorService.unfollowOrganization(organizationId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{spectatorId}/following-players")
    public ResponseEntity<Page<PlayerResponse>> getFollowingPlayers(
            @PathVariable Long spectatorId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PlayerResponse> followingPlayers = spectatorService.getFollowingPlayers(spectatorId, pageable);
        return ResponseEntity.ok(followingPlayers);
    }
    
    @GetMapping("/{spectatorId}/following-organizations")
    public ResponseEntity<Page<OrganizationResponse>> getFollowingOrganizations(
            @PathVariable Long spectatorId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrganizationResponse> followingOrganizations = spectatorService.getFollowingOrganizations(spectatorId, pageable);
        return ResponseEntity.ok(followingOrganizations);
    }
    
    @GetMapping("/players/{playerId}/is-following")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<Boolean> isFollowingPlayer(@PathVariable Long playerId) {
        boolean isFollowing = spectatorService.isFollowingPlayer(playerId);
        return ResponseEntity.ok(isFollowing);
    }
    
    @GetMapping("/organizations/{organizationId}/is-following")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<Boolean> isFollowingOrganization(@PathVariable Long organizationId) {
        boolean isFollowing = spectatorService.isFollowingOrganization(organizationId);
        return ResponseEntity.ok(isFollowing);
    }
    
    @GetMapping("/my-following-players")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<Page<PlayerResponse>> getMyFollowingPlayers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PlayerResponse> followingPlayers = spectatorService.getMyFollowingPlayers(pageable);
        return ResponseEntity.ok(followingPlayers);
    }
    
    @GetMapping("/my-following-organizations")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<Page<OrganizationResponse>> getMyFollowingOrganizations(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrganizationResponse> followingOrganizations = spectatorService.getMyFollowingOrganizations(pageable);
        return ResponseEntity.ok(followingOrganizations);
    }
}
