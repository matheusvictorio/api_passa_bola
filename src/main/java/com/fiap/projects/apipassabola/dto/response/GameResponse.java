package com.fiap.projects.apipassabola.dto.response;

import com.fiap.projects.apipassabola.entity.Game;
import com.fiap.projects.apipassabola.entity.GameType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameResponse {
    
    private Long id;
    private GameType gameType;
    
    // Fields for FRIENDLY and CHAMPIONSHIP games
    private String gameName;
    private String hostUsername;
    private Long hostId;
    private String description;
    private List<GameParticipantResponse> team1Players;
    private List<GameParticipantResponse> team2Players;
    
    // Game configuration
    private Boolean hasSpectators;
    private Integer minPlayers;
    private Integer maxPlayers;
    private Integer minSpectators;
    private Integer currentPlayerCount;
    private Integer team1Count;
    private Integer team2Count;
    private Boolean isTeamsBalanced;
    private Boolean canStart; // true if minimum players reached and teams balanced
    
    // Fields for CUP games (and backward compatibility)
    private OrganizationSummaryResponse homeTeam;
    private OrganizationSummaryResponse awayTeam;
    private String championship;
    private String round;
    
    // Common fields
    private LocalDateTime gameDate;
    private String venue;
    private Integer homeGoals;
    private Integer awayGoals;
    private Game.GameStatus status;
    private String notes;
    private String result;
    private OrganizationSummaryResponse winner; // For CUP games only
    private Integer winningTeamSide; // For FRIENDLY and CHAMPIONSHIP games (1 or 2)
    private boolean isDraw;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Helper methods for frontend
    public boolean isReadyToStart() {
        return canStart != null && canStart;
    }
}
