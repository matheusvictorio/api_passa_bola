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
    
    @ManyToOne
    @JoinColumn(name = "home_team_id", nullable = false)
    private Organization homeTeam;
    
    @ManyToOne
    @JoinColumn(name = "away_team_id", nullable = false)
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
            if (homeGoals > awayGoals) {
                return homeTeam;
            } else if (awayGoals > homeGoals) {
                return awayTeam;
            }
        }
        return null; // Draw or not finished
    }
    
    public boolean isDraw() {
        return status == GameStatus.FINISHED && homeGoals.equals(awayGoals);
    }
}
