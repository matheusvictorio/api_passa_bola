package com.fiap.projects.apipassabola.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for sending team invitations to CUP games
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameInviteRequest {
    
    @NotNull(message = "Game ID is required")
    private Long gameId;
    
    @NotNull(message = "Invited team ID is required")
    private Long invitedTeamId;
    
    @NotBlank(message = "Team position is required")
    @Pattern(regexp = "^(HOME|AWAY)$", message = "Team position must be HOME or AWAY")
    private String teamPosition;
    
    @Size(max = 500, message = "Message cannot exceed 500 characters")
    private String message;
}
