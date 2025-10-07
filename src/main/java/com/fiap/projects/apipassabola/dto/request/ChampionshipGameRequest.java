package com.fiap.projects.apipassabola.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for creating CHAMPIONSHIP games
 * hostUsername and hostId are automatically extracted from authenticated user context
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChampionshipGameRequest {
    
    @NotBlank(message = "Game name is required")
    private String gameName;
    
    @NotNull(message = "Game date is required")
    @Future(message = "Game date must be in the future")
    private LocalDateTime gameDate;
    
    @NotBlank(message = "Venue is required")
    private String venue;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Has spectators flag is required")
    private Boolean hasSpectators;
    
    @Min(value = 5, message = "Maximum spectators must be at least 5 when enabled")
    private Integer maxSpectators; // Optional: defaults to 5 if hasSpectators is true
    
    @Min(value = 6, message = "Minimum players must be at least 6 (3x3)")
    @Max(value = 22, message = "Maximum players cannot exceed 22 (11x11)")
    private Integer minPlayers = 6;
    
    @Min(value = 6, message = "Maximum players must be at least 6 (3x3)")
    @Max(value = 22, message = "Maximum players cannot exceed 22 (11x11)")
    private Integer maxPlayers = 22;
}
