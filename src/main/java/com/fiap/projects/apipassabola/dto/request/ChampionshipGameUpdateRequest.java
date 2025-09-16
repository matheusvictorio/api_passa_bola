package com.fiap.projects.apipassabola.dto.request;

import com.fiap.projects.apipassabola.entity.Game;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for updating CHAMPIONSHIP games
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChampionshipGameUpdateRequest {
    
    @NotBlank(message = "Game name is required")
    private String gameName;
    
    @NotNull(message = "Game date is required")
    private LocalDateTime gameDate;
    
    @NotBlank(message = "Venue is required")
    private String venue;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @Min(value = 0, message = "Home goals cannot be negative")
    private Integer homeGoals = 0;
    
    @Min(value = 0, message = "Away goals cannot be negative")
    private Integer awayGoals = 0;
    
    private Game.GameStatus status = Game.GameStatus.SCHEDULED;
    
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
