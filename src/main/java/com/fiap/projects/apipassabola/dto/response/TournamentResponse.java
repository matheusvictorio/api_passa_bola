package com.fiap.projects.apipassabola.dto.response;

import com.fiap.projects.apipassabola.entity.GameType;
import com.fiap.projects.apipassabola.entity.Tournament;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TournamentResponse {
    
    private Long id;
    private String name;
    private GameType gameType;
    private Long creatorId;
    private String creatorUsername;
    private Tournament.TournamentStatus status;
    private String description;
    private String venue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer totalTeams;
    private Integer maxTeams;
    private String currentRound;
    private Boolean bracketGenerated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Informações adicionais
    private List<TournamentTeamResponse> teams;
    private List<TournamentMatchResponse> matches;
}
