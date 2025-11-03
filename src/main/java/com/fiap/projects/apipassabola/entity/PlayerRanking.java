package com.fiap.projects.apipassabola.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_rankings", indexes = {
    @Index(name = "idx_player_ranking_points", columnList = "total_points DESC"),
    @Index(name = "idx_player_ranking_division", columnList = "division, total_points DESC")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRanking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    private Player player;
    
    @Column(name = "total_points", nullable = false)
    private Integer totalPoints = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Division division = Division.BRONZE;
    
    @Column(name = "games_won", nullable = false)
    private Integer gamesWon = 0;
    
    @Column(name = "games_drawn", nullable = false)
    private Integer gamesDrawn = 0;
    
    @Column(name = "games_lost", nullable = false)
    private Integer gamesLost = 0;
    
    @Column(name = "total_games", nullable = false)
    private Integer totalGames = 0;
    
    @Column(name = "win_rate")
    private Double winRate = 0.0;
    
    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak = 0; // Positivo para vitórias, negativo para derrotas
    
    @Column(name = "best_streak", nullable = false)
    private Integer bestStreak = 0;
    
    @Column(name = "last_game_date")
    private LocalDateTime lastGameDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        updateDivision();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        updateDivision();
        calculateWinRate();
    }
    
    /**
     * Adiciona pontos por vitória (3 pontos)
     */
    public void addWin() {
        totalPoints += 3;
        gamesWon++;
        totalGames++;
        updateStreak(true);
        lastGameDate = LocalDateTime.now();
    }
    
    /**
     * Adiciona pontos por empate (1 ponto)
     */
    public void addDraw() {
        totalPoints += 1;
        gamesDrawn++;
        totalGames++;
        currentStreak = 0; // Empate quebra a sequência
        lastGameDate = LocalDateTime.now();
    }
    
    /**
     * Registra derrota (0 pontos)
     */
    public void addLoss() {
        gamesLost++;
        totalGames++;
        updateStreak(false);
        lastGameDate = LocalDateTime.now();
    }
    
    /**
     * Adiciona pontos bônus (por gols marcados)
     */
    public void addBonusPoints(int points) {
        totalPoints += points;
        lastGameDate = LocalDateTime.now();
    }
    
    /**
     * Atualiza a divisão baseada nos pontos totais
     */
    private void updateDivision() {
        this.division = Division.fromPoints(totalPoints);
    }
    
    /**
     * Calcula a taxa de vitória
     */
    private void calculateWinRate() {
        if (totalGames > 0) {
            this.winRate = (double) gamesWon / totalGames * 100;
        } else {
            this.winRate = 0.0;
        }
    }
    
    /**
     * Atualiza a sequência de vitórias/derrotas
     */
    private void updateStreak(boolean isWin) {
        if (isWin) {
            if (currentStreak >= 0) {
                currentStreak++;
            } else {
                currentStreak = 1;
            }
            if (currentStreak > bestStreak) {
                bestStreak = currentStreak;
            }
        } else {
            if (currentStreak <= 0) {
                currentStreak--;
            } else {
                currentStreak = -1;
            }
        }
    }
    
    /**
     * Retorna pontos necessários para próxima divisão
     */
    public int getPointsToNextDivision() {
        return division.getPointsToNextDivision(totalPoints);
    }
}
