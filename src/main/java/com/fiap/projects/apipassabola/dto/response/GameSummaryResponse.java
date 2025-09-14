package com.fiap.projects.apipassabola.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSummaryResponse {
    
    private Long id;
    private String title;
    private String description;
    private LocalDateTime gameDate;
    private String location;
    private String homeTeamName;
    private String awayTeamName;
    private String status;
}
