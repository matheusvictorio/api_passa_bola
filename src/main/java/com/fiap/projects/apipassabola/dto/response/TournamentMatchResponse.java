package com.fiap.projects.apipassabola.dto.response;

import com.fiap.projects.apipassabola.entity.TournamentMatch;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TournamentMatchResponse {
    
    private Long id;
    private Long tournamentId;
    private String round;
    private Integer matchNumber;
    
    // Time 1
    private Long team1Id;
    private String team1Name;
    private Integer team1Score;
    
    // Time 2
    private Long team2Id;
    private String team2Name;
    private Integer team2Score;
    
    // Vencedor
    private Long winnerId;
    private String winnerName;
    
    private TournamentMatch.MatchStatus status;
    private LocalDateTime scheduledDate;
    private Integer bracketPosition;
    private Long nextMatchId;
    private Long gameId; // ID do jogo real se já foi criado
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
