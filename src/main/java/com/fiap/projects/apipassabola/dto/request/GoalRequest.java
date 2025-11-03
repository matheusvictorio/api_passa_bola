package com.fiap.projects.apipassabola.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalRequest {
    
    @NotNull(message = "Player ID is required")
    private Long playerId;
    
    @NotNull(message = "Team side is required")
    private Integer teamSide; // 1 ou 2
    
    private Integer minute; // Opcional
    
    private Boolean isOwnGoal = false; // Gol contra
}
