package com.fiap.projects.apipassabola.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponse {
    
    private Long id;
    private Long gameId;
    private Long playerId;
    private String playerName;
    private String playerUsername;
    private Integer teamSide;
    private Integer minute;
    private Boolean isOwnGoal;
    private LocalDateTime createdAt;
}
