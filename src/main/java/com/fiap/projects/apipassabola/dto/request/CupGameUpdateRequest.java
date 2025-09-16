package com.fiap.projects.apipassabola.dto.request;

import com.fiap.projects.apipassabola.entity.Game;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for updating CUP games (created by organizations)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CupGameUpdateRequest {
    
    @NotNull(message = "Home team is required")
    private Long homeTeamId;
    
    @NotNull(message = "Away team is required")
    private Long awayTeamId;
    
    @NotNull(message = "Game date is required")
    private LocalDateTime gameDate;
    
    @NotBlank(message = "Venue is required")
    private String venue;
    
    @NotBlank(message = "Championship is required")
    private String championship;
    
    @NotBlank(message = "Round is required")
    private String round;
    
    @Min(value = 0, message = "Home goals cannot be negative")
    private Integer homeGoals = 0;
    
    @Min(value = 0, message = "Away goals cannot be negative")
    private Integer awayGoals = 0;
    
    private Game.GameStatus status = Game.GameStatus.SCHEDULED;
    
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
