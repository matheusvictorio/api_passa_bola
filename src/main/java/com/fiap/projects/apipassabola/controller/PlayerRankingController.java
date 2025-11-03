package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.response.PlayerRankingResponse;
import com.fiap.projects.apipassabola.entity.Division;
import com.fiap.projects.apipassabola.service.PlayerRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para gerenciar rankings de jogadoras
 */
@RestController
@RequestMapping("/api/rankings/players")
@RequiredArgsConstructor
public class PlayerRankingController {
    
    private final PlayerRankingService playerRankingService;
    
    /**
     * Busca ranking de uma jogadora específica
     * GET /api/rankings/players/{playerId}
     */
    @GetMapping("/{playerId}")
    public ResponseEntity<PlayerRankingResponse> getPlayerRanking(@PathVariable Long playerId) {
        PlayerRankingResponse ranking = playerRankingService.getPlayerRanking(playerId);
        return ResponseEntity.ok(ranking);
    }
    
    /**
     * Busca ranking global de jogadoras
     * GET /api/rankings/players?page=0&size=50
     */
    @GetMapping
    public ResponseEntity<Page<PlayerRankingResponse>> getGlobalRanking(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PlayerRankingResponse> rankings = playerRankingService.getGlobalRanking(pageable);
        return ResponseEntity.ok(rankings);
    }
    
    /**
     * Busca ranking por divisão
     * GET /api/rankings/players/division/{division}?page=0&size=50
     */
    @GetMapping("/division/{division}")
    public ResponseEntity<Page<PlayerRankingResponse>> getRankingByDivision(
            @PathVariable Division division,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PlayerRankingResponse> rankings = playerRankingService.getRankingByDivision(division, pageable);
        return ResponseEntity.ok(rankings);
    }
    
    /**
     * Busca top jogadoras
     * GET /api/rankings/players/top?size=10
     */
    @GetMapping("/top")
    public ResponseEntity<Page<PlayerRankingResponse>> getTopPlayers(
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(0, size);
        Page<PlayerRankingResponse> rankings = playerRankingService.getTopPlayers(pageable);
        return ResponseEntity.ok(rankings);
    }
    
    /**
     * Busca jogadoras com melhor sequência de vitórias
     * GET /api/rankings/players/win-streak?size=10
     */
    @GetMapping("/win-streak")
    public ResponseEntity<Page<PlayerRankingResponse>> getPlayersWithWinStreak(
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(0, size);
        Page<PlayerRankingResponse> rankings = playerRankingService.getPlayersWithWinStreak(pageable);
        return ResponseEntity.ok(rankings);
    }
    
    /**
     * Busca jogadoras com maior taxa de vitória
     * GET /api/rankings/players/win-rate?minGames=10&size=10
     */
    @GetMapping("/win-rate")
    public ResponseEntity<Page<PlayerRankingResponse>> getPlayersByWinRate(
            @RequestParam(defaultValue = "10") Integer minGames,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(0, size);
        Page<PlayerRankingResponse> rankings = playerRankingService.getPlayersByWinRate(minGames, pageable);
        return ResponseEntity.ok(rankings);
    }
}
