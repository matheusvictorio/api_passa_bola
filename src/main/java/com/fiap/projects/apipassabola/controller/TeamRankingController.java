package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.response.TeamRankingResponse;
import com.fiap.projects.apipassabola.entity.Division;
import com.fiap.projects.apipassabola.service.TeamRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para gerenciar rankings de times
 */
@RestController
@RequestMapping("/api/rankings/teams")
@RequiredArgsConstructor
public class TeamRankingController {
    
    private final TeamRankingService teamRankingService;
    
    /**
     * Busca ranking de um time específico
     * GET /api/rankings/teams/{teamId}
     */
    @GetMapping("/{teamId}")
    public ResponseEntity<TeamRankingResponse> getTeamRanking(@PathVariable Long teamId) {
        TeamRankingResponse ranking = teamRankingService.getTeamRanking(teamId);
        return ResponseEntity.ok(ranking);
    }
    
    /**
     * Busca ranking global de times
     * GET /api/rankings/teams?page=0&size=50
     */
    @GetMapping
    public ResponseEntity<Page<TeamRankingResponse>> getGlobalRanking(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TeamRankingResponse> rankings = teamRankingService.getGlobalRanking(pageable);
        return ResponseEntity.ok(rankings);
    }
    
    /**
     * Busca ranking por divisão
     * GET /api/rankings/teams/division/{division}?page=0&size=50
     */
    @GetMapping("/division/{division}")
    public ResponseEntity<Page<TeamRankingResponse>> getRankingByDivision(
            @PathVariable Division division,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TeamRankingResponse> rankings = teamRankingService.getRankingByDivision(division, pageable);
        return ResponseEntity.ok(rankings);
    }
    
    /**
     * Busca top times
     * GET /api/rankings/teams/top?size=10
     */
    @GetMapping("/top")
    public ResponseEntity<Page<TeamRankingResponse>> getTopTeams(
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(0, size);
        Page<TeamRankingResponse> rankings = teamRankingService.getTopTeams(pageable);
        return ResponseEntity.ok(rankings);
    }
    
    /**
     * Busca times com melhor sequência de vitórias
     * GET /api/rankings/teams/win-streak?size=10
     */
    @GetMapping("/win-streak")
    public ResponseEntity<Page<TeamRankingResponse>> getTeamsWithWinStreak(
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(0, size);
        Page<TeamRankingResponse> rankings = teamRankingService.getTeamsWithWinStreak(pageable);
        return ResponseEntity.ok(rankings);
    }
    
    /**
     * Busca times com maior taxa de vitória
     * GET /api/rankings/teams/win-rate?minGames=10&size=10
     */
    @GetMapping("/win-rate")
    public ResponseEntity<Page<TeamRankingResponse>> getTeamsByWinRate(
            @RequestParam(defaultValue = "10") Integer minGames,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(0, size);
        Page<TeamRankingResponse> rankings = teamRankingService.getTeamsByWinRate(minGames, pageable);
        return ResponseEntity.ok(rankings);
    }
}
