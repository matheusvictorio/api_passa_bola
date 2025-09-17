package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.request.GameParticipationRequest;
import com.fiap.projects.apipassabola.dto.response.GameParticipantResponse;
import com.fiap.projects.apipassabola.service.GameParticipantService;
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
@RequestMapping("/api/game-participants")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GameParticipantController {
    
    private final GameParticipantService gameParticipantService;
    private final UserContextService userContextService;
    
    @PostMapping("/join")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<GameParticipantResponse> joinGame(@Valid @RequestBody GameParticipationRequest request) {
        // Get current player ID from security context
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        GameParticipantResponse participation = gameParticipantService.joinGame(currentUser.getUserId(), request);
        return ResponseEntity.ok(participation);
    }
    
    @DeleteMapping("/leave/{gameId}")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<Void> leaveGame(@PathVariable Long gameId) {
        // Get current player ID from security context
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        gameParticipantService.leaveGame(currentUser.getUserId(), gameId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<GameParticipantResponse>> getParticipantsByGame(@PathVariable Long gameId) {
        List<GameParticipantResponse> participants = gameParticipantService.getParticipantsByGame(gameId);
        return ResponseEntity.ok(participants);
    }
    
    @GetMapping("/player/{playerId}")
    public ResponseEntity<Page<GameParticipantResponse>> getParticipationsByPlayer(
            @PathVariable Long playerId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GameParticipantResponse> participations = gameParticipantService.getParticipationsByPlayer(playerId, pageable);
        return ResponseEntity.ok(participations);
    }
    
    @GetMapping("/my-participations")
    @PreAuthorize("hasRole('PLAYER') or hasRole('ORGANIZATION')")
    public ResponseEntity<Page<GameParticipantResponse>> getMyParticipations(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GameParticipantResponse> participations = gameParticipantService.getMyParticipations(pageable);
        return ResponseEntity.ok(participations);
    }
    
    @GetMapping("/team/{teamId}")
    public ResponseEntity<Page<GameParticipantResponse>> getParticipationsByTeam(
            @PathVariable Long teamId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GameParticipantResponse> participations = gameParticipantService.getParticipationsByTeam(teamId, pageable);
        return ResponseEntity.ok(participations);
    }
}
