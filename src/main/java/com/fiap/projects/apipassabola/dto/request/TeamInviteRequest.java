package com.fiap.projects.apipassabola.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeamInviteRequest {
    
    @NotNull(message = "Invited player ID is required")
    private Long invitedPlayerId;
}
