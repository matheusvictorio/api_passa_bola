package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.response.GoalResponse;
import com.fiap.projects.apipassabola.entity.Goal;
import com.fiap.projects.apipassabola.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {
    
    private final GoalRepository goalRepository;
    
    @Transactional(readOnly = true)
    public List<GoalResponse> getGoalsByGame(Long gameId) {
        return goalRepository.findGoalsByGame(gameId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<GoalResponse> getGoalsByPlayer(Long playerId) {
        return goalRepository.findGoalsByPlayerOrderByDate(playerId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Long countGoalsByPlayer(Long playerId) {
        return goalRepository.countGoalsByPlayer(playerId);
    }
    
    private GoalResponse convertToResponse(Goal goal) {
        GoalResponse response = new GoalResponse();
        response.setId(goal.getId());
        response.setGameId(goal.getGame().getId());
        response.setPlayerId(goal.getPlayer().getId());
        response.setPlayerName(goal.getPlayer().getName());
        response.setPlayerUsername(goal.getPlayer().getRealUsername());
        response.setTeamSide(goal.getTeamSide());
        response.setMinute(goal.getMinute());
        response.setIsOwnGoal(goal.getIsOwnGoal());
        response.setCreatedAt(goal.getCreatedAt());
        return response;
    }
}
