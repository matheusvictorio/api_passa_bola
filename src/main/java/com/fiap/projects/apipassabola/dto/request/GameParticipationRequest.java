package com.fiap.projects.apipassabola.dto.request;

import com.fiap.projects.apipassabola.entity.GameParticipant;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for joining FRIENDLY or CHAMPIONSHIP games
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameParticipationRequest {
    
    @NotNull(message = "Game ID is required")
    private Long gameId;
    
    @NotNull(message = "Participation type is required")
    private GameParticipant.ParticipationType participationType;
    
    @Min(value = 1, message = "Team side must be 1 or 2")
    @Max(value = 2, message = "Team side must be 1 or 2")
    private Integer teamSide; // 1 for team 1, 2 for team 2
}
