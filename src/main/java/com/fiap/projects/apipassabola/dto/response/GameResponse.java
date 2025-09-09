package com.fiap.projects.apipassabola.dto.response;

import com.fiap.projects.apipassabola.entity.Game;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameResponse {
    
    private Long id;
    private OrganizationSummaryResponse homeTeam;
    private OrganizationSummaryResponse awayTeam;
    private LocalDateTime gameDate;
    private String venue;
    private Integer homeGoals;
    private Integer awayGoals;
    private Game.GameStatus status;
    private String championship;
    private String round;
    private String notes;
    private String result;
    private OrganizationSummaryResponse winner;
    private boolean isDraw;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
