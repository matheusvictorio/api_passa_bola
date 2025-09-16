package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.request.PlayerRequest;
import com.fiap.projects.apipassabola.dto.response.OrganizationResponse;
import com.fiap.projects.apipassabola.dto.response.PlayerResponse;
import com.fiap.projects.apipassabola.dto.response.SpectatorResponse;
import com.fiap.projects.apipassabola.entity.Player;
import com.fiap.projects.apipassabola.service.PlayerService;
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
@RequestMapping("/api/players")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlayerController {
    
    private final PlayerService playerService;
    
    @GetMapping
    public ResponseEntity<Page<PlayerResponse>> getAllPlayers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PlayerResponse> players = playerService.findAll(pageable);
        return ResponseEntity.ok(players);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponse> getPlayerById(@PathVariable Long id) {
        PlayerResponse player = playerService.findById(id);
        return ResponseEntity.ok(player);
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<PlayerResponse> getPlayerByUsername(@PathVariable String username) {
        PlayerResponse player = playerService.findByUsername(username);
        return ResponseEntity.ok(player);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<PlayerResponse>> searchPlayersByName(
            @RequestParam String name,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PlayerResponse> players = playerService.findByName(name, pageable);
        return ResponseEntity.ok(players);
    }
    
    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<Page<PlayerResponse>> getPlayersByOrganization(
            @PathVariable Long organizationId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PlayerResponse> players = playerService.findByOrganization(organizationId, pageable);
        return ResponseEntity.ok(players);
    }
    
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PLAYER') or hasRole('ORGANIZATION')")
    public ResponseEntity<PlayerResponse> updatePlayer(
            @PathVariable Long id,
            @Valid @RequestBody PlayerRequest request) {
        PlayerResponse updatedPlayer = playerService.update(id, request);
        return ResponseEntity.ok(updatedPlayer);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PLAYER') or hasRole('ORGANIZATION')")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        playerService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/follow")
    @PreAuthorize("hasRole('PLAYER') or hasRole('SPECTATOR')")
    public ResponseEntity<PlayerResponse> followPlayer(
            @PathVariable Long id,
            Authentication authentication) {
        String currentUserEmail = authentication.getName();
        PlayerResponse followedPlayer = playerService.followByEmail(currentUserEmail, id);
        return ResponseEntity.ok(followedPlayer);
    }
    
    @PostMapping("/{followerId}/follow/{followedId}")
    @PreAuthorize("hasRole('PLAYER') or hasRole('SPECTATOR')")
    public ResponseEntity<PlayerResponse> followPlayerByIds(
            @PathVariable Long followerId,
            @PathVariable Long followedId) {
        PlayerResponse followedPlayer = playerService.follow(followerId, followedId);
        return ResponseEntity.ok(followedPlayer);
    }
    
    @DeleteMapping("/{id}/follow")
    @PreAuthorize("hasRole('PLAYER') or hasRole('SPECTATOR')")
    public ResponseEntity<PlayerResponse> unfollowPlayer(
            @PathVariable Long id,
            Authentication authentication) {
        String currentUserEmail = authentication.getName();
        PlayerResponse unfollowedPlayer = playerService.unfollowByEmail(currentUserEmail, id);
        return ResponseEntity.ok(unfollowedPlayer);
    }
    
    @DeleteMapping("/{followerId}/follow/{followedId}")
    @PreAuthorize("hasRole('PLAYER') or hasRole('SPECTATOR')")
    public ResponseEntity<PlayerResponse> unfollowPlayerByIds(
            @PathVariable Long followerId,
            @PathVariable Long followedId) {
        PlayerResponse unfollowedPlayer = playerService.unfollow(followerId, followedId);
        return ResponseEntity.ok(unfollowedPlayer);
    }
    
    @GetMapping("/{id}/followers")
    public ResponseEntity<Page<PlayerResponse>> getPlayerFollowers(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PlayerResponse> followers = playerService.getFollowers(id, pageable);
        return ResponseEntity.ok(followers);
    }
    
    @GetMapping("/{id}/following")
    public ResponseEntity<Page<PlayerResponse>> getPlayerFollowing(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PlayerResponse> following = playerService.getFollowing(id, pageable);
        return ResponseEntity.ok(following);
    }
    
    // ========== CROSS-TYPE FOLLOWING ENDPOINTS FOR PLAYER ==========
    
    // Player following Spectators
    @PostMapping("/spectators/{spectatorId}/follow")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<Void> followSpectator(
            @PathVariable Long spectatorId,
            Authentication authentication) {
        Player player = (Player) authentication.getPrincipal();
        playerService.followSpectator(player.getId(), spectatorId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/spectators/{spectatorId}/follow")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<Void> unfollowSpectator(
            @PathVariable Long spectatorId,
            Authentication authentication) {
        Player player = (Player) authentication.getPrincipal();
        playerService.unfollowSpectator(player.getId(), spectatorId);
        return ResponseEntity.ok().build();
    }
    
    // Player following Organizations
    @PostMapping("/organizations/{organizationId}/follow")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<Void> followOrganization(
            @PathVariable Long organizationId,
            Authentication authentication) {
        Player player = (Player) authentication.getPrincipal();
        playerService.followOrganization(player.getId(), organizationId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/organizations/{organizationId}/follow")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<Void> unfollowOrganization(
            @PathVariable Long organizationId,
            Authentication authentication) {
        Player player = (Player) authentication.getPrincipal();
        playerService.unfollowOrganization(player.getId(), organizationId);
        return ResponseEntity.ok().build();
    }
    
    // Get following lists for Player (cross-type)
    @GetMapping("/{id}/following-spectators")
    public ResponseEntity<Page<SpectatorResponse>> getPlayerFollowingSpectators(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SpectatorResponse> followingSpectators = playerService.getFollowingSpectators(id, pageable);
        return ResponseEntity.ok(followingSpectators);
    }
    
    @GetMapping("/{id}/following-organizations")
    public ResponseEntity<Page<OrganizationResponse>> getPlayerFollowingOrganizations(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrganizationResponse> followingOrganizations = playerService.getFollowingOrganizations(id, pageable);
        return ResponseEntity.ok(followingOrganizations);
    }
    
    // Check following status for Player (cross-type)
    @GetMapping("/spectators/{spectatorId}/is-following")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<Boolean> isFollowingSpectator(
            @PathVariable Long spectatorId,
            Authentication authentication) {
        Player player = (Player) authentication.getPrincipal();
        boolean isFollowing = playerService.isFollowingSpectator(player.getId(), spectatorId);
        return ResponseEntity.ok(isFollowing);
    }
    
    @GetMapping("/organizations/{organizationId}/is-following")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<Boolean> isFollowingOrganization(
            @PathVariable Long organizationId,
            Authentication authentication) {
        Player player = (Player) authentication.getPrincipal();
        boolean isFollowing = playerService.isFollowingOrganization(player.getId(), organizationId);
        return ResponseEntity.ok(isFollowing);
    }
    
    // Get followers lists for Player (cross-type)
    @GetMapping("/{id}/spectator-followers")
    public ResponseEntity<Page<SpectatorResponse>> getPlayerSpectatorFollowers(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SpectatorResponse> spectatorFollowers = playerService.getSpectatorFollowers(id, pageable);
        return ResponseEntity.ok(spectatorFollowers);
    }
    
    @GetMapping("/{id}/organization-followers")
    public ResponseEntity<Page<OrganizationResponse>> getPlayerOrganizationFollowers(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrganizationResponse> organizationFollowers = playerService.getOrganizationFollowers(id, pageable);
        return ResponseEntity.ok(organizationFollowers);
    }
    
    // Personal following lists for authenticated Player
    @GetMapping("/my-following-spectators")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<Page<SpectatorResponse>> getMyFollowingSpectators(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        Player player = (Player) authentication.getPrincipal();
        Page<SpectatorResponse> followingSpectators = playerService.getFollowingSpectators(player.getId(), pageable);
        return ResponseEntity.ok(followingSpectators);
    }
    
    @GetMapping("/my-following-organizations")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<Page<OrganizationResponse>> getMyFollowingOrganizations(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        Player player = (Player) authentication.getPrincipal();
        Page<OrganizationResponse> followingOrganizations = playerService.getFollowingOrganizations(player.getId(), pageable);
        return ResponseEntity.ok(followingOrganizations);
    }
}
