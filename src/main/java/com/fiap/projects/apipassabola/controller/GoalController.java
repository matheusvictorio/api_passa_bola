package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.response.GoalResponse;
import com.fiap.projects.apipassabola.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciar gols
 */
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GoalController {
    
    private final GoalService goalService;
    
    /**
     * Busca todos os gols de um jogo
     * GET /api/goals/game/{gameId}
     */
    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<GoalResponse>> getGoalsByGame(@PathVariable Long gameId) {
        List<GoalResponse> goals = goalService.getGoalsByGame(gameId);
        return ResponseEntity.ok(goals);
    }
    
    /**
     * Busca todos os gols de uma jogadora
     * GET /api/goals/player/{playerId}
     */
    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<GoalResponse>> getGoalsByPlayer(@PathVariable Long playerId) {
        List<GoalResponse> goals = goalService.getGoalsByPlayer(playerId);
        return ResponseEntity.ok(goals);
    }
    
    /**
     * Conta total de gols de uma jogadora
     * GET /api/goals/player/{playerId}/count
     */
    @GetMapping("/player/{playerId}/count")
    public ResponseEntity<Long> countGoalsByPlayer(@PathVariable Long playerId) {
        Long count = goalService.countGoalsByPlayer(playerId);
        return ResponseEntity.ok(count);
    }
}
