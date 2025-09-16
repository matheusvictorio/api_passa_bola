package com.fiap.projects.apipassabola.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "games")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "game_type", nullable = false)
    private GameType gameType;
    
    // Fields for FRIENDLY and CHAMPIONSHIP games
    @Column(name = "game_name")
    private String gameName;
    
    @Column(name = "host_username")
    private String hostUsername;
    
    @Column(name = "host_id")
    private Long hostId;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    // Fields for CUP games (and backward compatibility)
    @ManyToOne
    @JoinColumn(name = "home_team_id")
    private Organization homeTeam;
    
    @ManyToOne
    @JoinColumn(name = "away_team_id")
    private Organization awayTeam;
    
    @Column(name = "game_date", nullable = false)
    private LocalDateTime gameDate;
    
    @Column(nullable = false)
    private String venue;
    
    @Column(name = "home_goals")
    private Integer homeGoals = 0;
    
    @Column(name = "away_goals")
    private Integer awayGoals = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status = GameStatus.SCHEDULED;
    
    private String championship;
    
    private String round;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum GameStatus {
        SCHEDULED, LIVE, FINISHED, POSTPONED, CANCELLED
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public String getResult() {
        if (status == GameStatus.FINISHED) {
            return homeGoals + " - " + awayGoals;
        }
        return "Not finished";
    }
    
    public Organization getWinner() {
        if (status == GameStatus.FINISHED) {
            // For CUP games, return the winning organization only if they are different
            if (gameType == GameType.CUP && homeTeam != null && awayTeam != null) {
                // If both teams belong to the same organization, we can't determine an organization winner
                if (homeTeam != null && awayTeam != null && homeTeam.equals(awayTeam)) {
                    return null; // Same organization, use getWinningTeamSide() instead
                }
                
                // Different organizations, return the winning one
                if (homeGoals > awayGoals) {
                    return homeTeam;
                } else if (awayGoals > homeGoals) {
                    return awayTeam;
                }
            }
            // For FRIENDLY and CHAMPIONSHIP games, we can't return an Organization winner
            // since they're played by individual players or mixed teams
            // The winner determination should be handled at the service layer
        }
        return null; // Draw, not finished, or same organization in cup game
    }
    
    /**
     * Determines which team side won (1 or 2) for FRIENDLY and CHAMPIONSHIP games
     * @return 1 if team 1 won, 2 if team 2 won, null if draw or not finished
     */
    public Integer getWinningTeamSide() {
        if (status == GameStatus.FINISHED) {
            if (homeGoals > awayGoals) {
                return 1; // Team 1 (home side) won
            } else if (awayGoals > homeGoals) {
                return 2; // Team 2 (away side) won
            }
        }
        return null; // Draw or not finished
    }
    
    public boolean isDraw() {
        return status == GameStatus.FINISHED && homeGoals.equals(awayGoals);
    }
    
    public boolean isCupGame() {
        return gameType == GameType.CUP;
    }
    
    public boolean isFriendlyOrChampionship() {
        return gameType == GameType.FRIENDLY || gameType == GameType.CHAMPIONSHIP;
    }
    
    public boolean requiresTeams() {
        return gameType == GameType.CUP;
    }
    
    public boolean allowsIndividualParticipation() {
        return gameType == GameType.FRIENDLY || gameType == GameType.CHAMPIONSHIP;
    }
}
