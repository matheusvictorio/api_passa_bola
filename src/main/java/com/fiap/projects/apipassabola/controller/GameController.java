package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.request.GameRequest;
import com.fiap.projects.apipassabola.dto.response.GameResponse;
import com.fiap.projects.apipassabola.entity.Game;
import com.fiap.projects.apipassabola.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GameController {
    
    private final GameService gameService;
    
    @GetMapping
    public ResponseEntity<Page<GameResponse>> getAllGames(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GameResponse> games = gameService.findAll(pageable);
        return ResponseEntity.ok(games);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> getGameById(@PathVariable Long id) {
        GameResponse game = gameService.findById(id);
        return ResponseEntity.ok(game);
    }
    
    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<Page<GameResponse>> getGamesByOrganization(
            @PathVariable Long organizationId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GameResponse> games = gameService.findByOrganization(organizationId, pageable);
        return ResponseEntity.ok(games);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<GameResponse>> getGamesByStatus(
            @PathVariable Game.GameStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GameResponse> games = gameService.findByStatus(status, pageable);
        return ResponseEntity.ok(games);
    }
    
    @GetMapping("/championship")
    public ResponseEntity<Page<GameResponse>> getGamesByChampionship(
            @RequestParam String championship,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GameResponse> games = gameService.findByChampionship(championship, pageable);
        return ResponseEntity.ok(games);
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<Page<GameResponse>> getGamesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GameResponse> games = gameService.findByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(games);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<GameResponse> createGame(@Valid @RequestBody GameRequest request) {
        GameResponse createdGame = gameService.create(request);
        return ResponseEntity.ok(createdGame);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<GameResponse> updateGame(
            @PathVariable Long id,
            @Valid @RequestBody GameRequest request) {
        GameResponse updatedGame = gameService.update(id, request);
        return ResponseEntity.ok(updatedGame);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<Void> deleteGame(@PathVariable Long id) {
        gameService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/score")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<GameResponse> updateGameScore(
            @PathVariable Long id,
            @RequestParam Integer homeGoals,
            @RequestParam Integer awayGoals) {
        GameResponse updatedGame = gameService.updateScore(id, homeGoals, awayGoals);
        return ResponseEntity.ok(updatedGame);
    }
}
