package com.fiap.projects.apipassabola.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeamResponse {
    
    private Long id;
    private String nameTeam;
    private PlayerSummaryResponse leader;
    private List<PlayerSummaryResponse> players;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int playerCount;
}
