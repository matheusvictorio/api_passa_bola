package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.response.GameSpectatorResponse;
import com.fiap.projects.apipassabola.service.GameSpectatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GameSpectatorController {
    
    private final GameSpectatorService gameSpectatorService;
    
    /**
     * POST /api/games/{id}/spectate - Confirmar presença como espectador
     */
    @PostMapping("/{id}/spectate")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<GameSpectatorResponse> joinGameAsSpectator(@PathVariable Long id) {
        GameSpectatorResponse response = gameSpectatorService.joinGame(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE /api/games/{id}/spectate - Cancelar presença como espectador
     */
    @DeleteMapping("/{id}/spectate")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<Void> leaveGameAsSpectator(@PathVariable Long id) {
        gameSpectatorService.leaveGame(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * GET /api/games/{id}/spectators - Lista de espectadores confirmados
     */
    @GetMapping("/{id}/spectators")
    public ResponseEntity<List<GameSpectatorResponse>> getGameSpectators(@PathVariable Long id) {
        List<GameSpectatorResponse> spectators = gameSpectatorService.getSpectatorsByGame(id);
        return ResponseEntity.ok(spectators);
    }
    
    /**
     * GET /api/games/{id}/spectators/count - Contagem de espectadores confirmados
     */
    @GetMapping("/{id}/spectators/count")
    public ResponseEntity<Long> getSpectatorCount(@PathVariable Long id) {
        long count = gameSpectatorService.getConfirmedSpectatorCount(id);
        return ResponseEntity.ok(count);
    }
    
    /**
     * GET /api/games/{id}/spectators/is-subscribed - Verificar se está inscrito
     */
    @GetMapping("/{id}/spectators/is-subscribed")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<Boolean> isSubscribed(@PathVariable Long id) {
        boolean subscribed = gameSpectatorService.isSubscribed(id);
        return ResponseEntity.ok(subscribed);
    }
    
    /**
     * GET /api/games/spectators/my-subscriptions - Meus jogos inscritos
     */
    @GetMapping("/spectators/my-subscriptions")
    @PreAuthorize("hasRole('SPECTATOR')")
    public ResponseEntity<Page<GameSpectatorResponse>> getMySubscriptions(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GameSpectatorResponse> subscriptions = gameSpectatorService.getMySubscribedGames(pageable);
        return ResponseEntity.ok(subscriptions);
    }
}
