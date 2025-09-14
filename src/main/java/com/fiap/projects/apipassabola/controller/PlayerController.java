package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.request.PlayerRequest;
import com.fiap.projects.apipassabola.dto.response.PlayerResponse;
import com.fiap.projects.apipassabola.entity.Player;
import com.fiap.projects.apipassabola.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    
    @PostMapping("/{followerId}/follow/{followedId}")
    @PreAuthorize("hasRole('PLAYER') or hasRole('SPECTATOR')")
    public ResponseEntity<PlayerResponse> followPlayer(
            @PathVariable Long followerId,
            @PathVariable Long followedId) {
        PlayerResponse followedPlayer = playerService.follow(followerId, followedId);
        return ResponseEntity.ok(followedPlayer);
    }
    
    @DeleteMapping("/{followerId}/follow/{followedId}")
    @PreAuthorize("hasRole('PLAYER') or hasRole('SPECTATOR')")
    public ResponseEntity<PlayerResponse> unfollowPlayer(
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
}
