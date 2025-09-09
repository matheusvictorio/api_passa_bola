package com.fiap.projects.apipassabola.dto.request;

import com.fiap.projects.apipassabola.entity.Game;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameRequest {
    
    @NotNull(message = "Home team is required")
    private Long homeTeamId;
    
    @NotNull(message = "Away team is required")
    private Long awayTeamId;
    
    @NotNull(message = "Game date is required")
    @Future(message = "Game date must be in the future")
    private LocalDateTime gameDate;
    
    @NotBlank(message = "Venue is required")
    private String venue;
    
    @Min(value = 0, message = "Home goals cannot be negative")
    private Integer homeGoals = 0;
    
    @Min(value = 0, message = "Away goals cannot be negative")
    private Integer awayGoals = 0;
    
    private Game.GameStatus status = Game.GameStatus.SCHEDULED;
    
    private String championship;
    
    private String round;
    
    private String notes;
}
