package com.fiap.projects.apipassabola.dto.response;

import lombok.Data;

@Data
public class TeamSummaryResponse {
    
    private Long id;
    private String nameTeam;
    private String leaderUsername;
    private int playerCount;
}
