package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.request.GameInviteRequest;
import com.fiap.projects.apipassabola.dto.response.GameInviteResponse;
import com.fiap.projects.apipassabola.service.GameInviteService;
import com.fiap.projects.apipassabola.service.UserContextService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game-invites")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GameInviteController {
    
    private final GameInviteService gameInviteService;
    private final UserContextService userContextService;
    
    @PostMapping("/send")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<GameInviteResponse> sendInvite(@Valid @RequestBody GameInviteRequest request) {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        GameInviteResponse invite = gameInviteService.sendInvite(currentUser.getUserId(), request);
        return ResponseEntity.ok(invite);
    }
    
    @PostMapping("/accept/{inviteId}")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<GameInviteResponse> acceptInvite(@PathVariable Long inviteId) {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        GameInviteResponse invite = gameInviteService.acceptInvite(currentUser.getUserId(), inviteId);
        return ResponseEntity.ok(invite);
    }
    
    @PostMapping("/reject/{inviteId}")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<GameInviteResponse> rejectInvite(@PathVariable Long inviteId) {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        GameInviteResponse invite = gameInviteService.rejectInvite(currentUser.getUserId(), inviteId);
        return ResponseEntity.ok(invite);
    }
    
    @DeleteMapping("/cancel/{inviteId}")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<Void> cancelInvite(@PathVariable Long inviteId) {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        gameInviteService.cancelInvite(currentUser.getUserId(), inviteId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<GameInviteResponse>> getInvitesByGame(@PathVariable Long gameId) {
        List<GameInviteResponse> invites = gameInviteService.getInvitesByGame(gameId);
        return ResponseEntity.ok(invites);
    }
    
    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<Page<GameInviteResponse>> getInvitesByOrganization(
            @PathVariable Long organizationId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GameInviteResponse> invites = gameInviteService.getInvitesByOrganization(organizationId, pageable);
        return ResponseEntity.ok(invites);
    }
    
    @GetMapping("/team/{teamId}")
    public ResponseEntity<Page<GameInviteResponse>> getInvitesByTeam(
            @PathVariable Long teamId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GameInviteResponse> invites = gameInviteService.getInvitesByTeam(teamId, pageable);
        return ResponseEntity.ok(invites);
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<List<GameInviteResponse>> getPendingInvites() {
        List<GameInviteResponse> invites = gameInviteService.getPendingInvitesForCurrentOrganization();
        return ResponseEntity.ok(invites);
    }
    
    @GetMapping("/sent")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<Page<GameInviteResponse>> getSentInvites(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GameInviteResponse> invites = gameInviteService.getSentInvitesByCurrentOrganization(pageable);
        return ResponseEntity.ok(invites);
    }
}
