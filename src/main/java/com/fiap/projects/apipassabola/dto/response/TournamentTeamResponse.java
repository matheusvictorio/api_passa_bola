package com.fiap.projects.apipassabola.dto.response;

import com.fiap.projects.apipassabola.entity.TournamentTeam;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TournamentTeamResponse {
    
    private Long id;
    private Long tournamentId;
    private Long teamId;
    private String teamName;
    private Integer seedPosition;
    private TournamentTeam.TeamStatus status;
    private LocalDateTime registeredAt;
}
