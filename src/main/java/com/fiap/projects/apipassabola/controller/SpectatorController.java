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
}
