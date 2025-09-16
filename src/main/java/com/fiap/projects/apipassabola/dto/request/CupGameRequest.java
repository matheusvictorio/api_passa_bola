package com.fiap.projects.apipassabola.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for creating CUP games (created by organizations)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CupGameRequest {
    
    @NotNull(message = "Home team is required")
    private Long homeTeamId;
    
    @NotNull(message = "Away team is required")
    private Long awayTeamId;
    
    @NotNull(message = "Game date is required")
    @Future(message = "Game date must be in the future")
    private LocalDateTime gameDate;
    
    @NotBlank(message = "Venue is required")
    private String venue;
    
    @NotBlank(message = "Championship is required")
    private String championship;
    
    @NotBlank(message = "Round is required")
    private String round;
}
