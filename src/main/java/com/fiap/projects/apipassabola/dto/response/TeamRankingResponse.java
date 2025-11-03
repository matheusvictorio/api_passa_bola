package com.fiap.projects.apipassabola.dto.response;

import com.fiap.projects.apipassabola.entity.Division;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamRankingResponse {
    
    private Long id;
    private Long teamId;
    private String teamName;
    private String leaderName;
    private Long leaderId;
    private Integer playersCount;
    private Integer totalPoints;
    private Division division;
    private String divisionName;
    private Integer gamesWon;
    private Integer gamesDrawn;
    private Integer gamesLost;
    private Integer totalGames;
    private Double winRate;
    private Integer currentStreak;
    private Integer bestStreak;
    private Long globalPosition;
    private Long divisionPosition;
    private Integer pointsToNextDivision;
    private LocalDateTime lastGameDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
