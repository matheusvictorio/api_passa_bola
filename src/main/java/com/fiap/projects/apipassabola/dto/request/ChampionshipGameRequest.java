package com.fiap.projects.apipassabola.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for creating CHAMPIONSHIP games
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChampionshipGameRequest {
    
    @NotBlank(message = "Game name is required")
    private String gameName;
    
    @NotBlank(message = "Host username is required")
    private String hostUsername;
    
    @NotNull(message = "Host ID is required")
    private Long hostId;
    
    @NotNull(message = "Game date is required")
    @Future(message = "Game date must be in the future")
    private LocalDateTime gameDate;
    
    @NotBlank(message = "Venue is required")
    private String venue;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
}
