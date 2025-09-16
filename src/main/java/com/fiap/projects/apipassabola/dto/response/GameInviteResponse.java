package com.fiap.projects.apipassabola.dto.response;

import com.fiap.projects.apipassabola.entity.GameInvite;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for GameInvite responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameInviteResponse {
    
    private Long id;
    private Long gameId;
    private String gameName;
    private OrganizationSummaryResponse invitingOrganization;
    private TeamSummaryResponse invitedTeam;
    private GameInvite.InviteStatus status;
    private String teamPosition;
    private String message;
    private LocalDateTime invitedAt;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
