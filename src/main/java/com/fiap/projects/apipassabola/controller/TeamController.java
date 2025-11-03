package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.request.TeamRequest;
import com.fiap.projects.apipassabola.dto.request.TeamInviteRequest;
import com.fiap.projects.apipassabola.dto.response.TeamResponse;
import com.fiap.projects.apipassabola.dto.response.TeamInviteResponse;
import com.fiap.projects.apipassabola.entity.Team;
import com.fiap.projects.apipassabola.entity.TeamInvite;
import com.fiap.projects.apipassabola.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {
    
    private final TeamService teamService;
    
    // Create a new team (only players can create teams)
    @PostMapping
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody TeamRequest request) {
        Team team = teamService.createTeam(request.getNameTeam());
        TeamResponse response = teamService.convertToResponse(team);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    // Get team by ID (public endpoint)
    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> getTeam(@PathVariable Long id) {
        Team team = teamService.findById(id);
        TeamResponse response = teamService.convertToResponse(team);
        return ResponseEntity.ok(response);
    }
    
    // Get all teams with pagination (public endpoint)
    @GetMapping
    public ResponseEntity<Page<TeamResponse>> getAllTeams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Team> teams = teamService.findAll(pageable);
        Page<TeamResponse> response = teams.map(teamService::convertToResponse);
        
        return ResponseEntity.ok(response);
    }
    
    // Search teams by name (public endpoint)
    @GetMapping("/search")
    public ResponseEntity<Page<TeamResponse>> searchTeams(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("nameTeam").ascending());
        Page<Team> teams = teamService.searchByName(name, pageable);
        Page<TeamResponse> response = teams.map(teamService::convertToResponse);
        
        return ResponseEntity.ok(response);
    }
    
    // Send invite to player (only team leader can send invites)
    @PostMapping("/{teamId}/invites")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<TeamInviteResponse> sendInvite(
            @PathVariable Long teamId,
            @Valid @RequestBody TeamInviteRequest request) {
        
        TeamInvite invite = teamService.sendInvite(teamId, request.getInvitedPlayerId());
        TeamInviteResponse response = teamService.convertInviteToResponse(invite);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    // Get pending invites for team (only team leader can view)
    @GetMapping("/{teamId}/invites")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<List<TeamInviteResponse>> getTeamInvites(@PathVariable Long teamId) {
        List<TeamInvite> invites = teamService.getTeamPendingInvites(teamId);
        List<TeamInviteResponse> response = invites.stream()
                .map(teamService::convertInviteToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    // Cancel invite (only team leader can cancel)
    @DeleteMapping("/invites/{inviteId}")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<Void> cancelInvite(@PathVariable Long inviteId) {
        teamService.cancelInvite(inviteId);
        return ResponseEntity.noContent().build();
    }
    
    // Accept invite (only invited player can accept)
    @PostMapping("/invites/{inviteId}/accept")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<TeamResponse> acceptInvite(@PathVariable Long inviteId) {
        Team team = teamService.acceptInvite(inviteId);
        TeamResponse response = teamService.convertToResponse(team);
        return ResponseEntity.ok(response);
    }
    
    // Reject invite (only invited player can reject)
    @PostMapping("/invites/{inviteId}/reject")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<Void> rejectInvite(@PathVariable Long inviteId) {
        teamService.rejectInvite(inviteId);
        return ResponseEntity.noContent().build();
    }
    
    // Get my pending invites (authenticated player)
    @GetMapping("/my-invites")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<List<TeamInviteResponse>> getMyInvites() {
        List<TeamInvite> invites = teamService.getMyPendingInvites();
        List<TeamInviteResponse> response = invites.stream()
                .map(teamService::convertInviteToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    // Get all teams that the current player belongs to
    @GetMapping("/my-teams")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<List<TeamResponse>> getMyTeams() {
        List<Team> teams = teamService.getMyTeams();
        List<TeamResponse> response = teams.stream()
                .map(teamService::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    // Leave team (authenticated player)
    @PostMapping("/{teamId}/leave")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<Void> leaveTeam(@PathVariable Long teamId) {
        teamService.leaveTeam(teamId);
        return ResponseEntity.noContent().build();
    }
    
    // Remove player from team (only team leader can remove)
    @DeleteMapping("/{teamId}/players/{playerId}")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<Void> removePlayer(
            @PathVariable Long teamId,
            @PathVariable Long playerId) {
        teamService.removePlayerFromTeam(teamId, playerId);
        return ResponseEntity.noContent().build();
    }
}
