package com.fiap.projects.apipassabola.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeamInviteResponse {
    
    private Long id;
    private TeamSummaryResponse team;
    private PlayerSummaryResponse inviter;
    private PlayerSummaryResponse invitedPlayer;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
