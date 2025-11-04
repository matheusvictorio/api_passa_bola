package com.fiap.projects.apipassabola.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tournament_matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentMatch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;
    
    @Column(nullable = false)
    private String round; // "FINAL", "SEMI", "QUARTER", etc.
    
    @Column(name = "match_number", nullable = false)
    private Integer matchNumber; // Número da partida dentro da rodada (1, 2, 3, 4...)
    
    @ManyToOne
    @JoinColumn(name = "team1_id")
    private Team team1;
    
    @ManyToOne
    @JoinColumn(name = "team2_id")
    private Team team2;
    
    @OneToOne
    @JoinColumn(name = "game_id")
    private Game game; // Referência ao jogo real quando criado
    
    @Column(name = "team1_score")
    private Integer team1Score;
    
    @Column(name = "team2_score")
    private Integer team2Score;
    
    @ManyToOne
    @JoinColumn(name = "winner_id")
    private Team winner;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status = MatchStatus.PENDING;
    
    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;
    
    @Column(name = "bracket_position")
    private Integer bracketPosition; // Posição no chaveamento para visualização
    
    @Column(name = "next_match_id")
    private Long nextMatchId; // ID da próxima partida (para o vencedor)
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum MatchStatus {
        PENDING,      // Aguardando times (quando depende de partidas anteriores)
        SCHEDULED,    // Times definidos, aguardando jogo
        IN_PROGRESS,  // Jogo em andamento
        FINISHED,     // Jogo finalizado
        WALKOVER      // W.O. (um time não compareceu)
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
    public boolean isReady() {
        return team1 != null && team2 != null;
    }
    
    public boolean isFinished() {
        return status == MatchStatus.FINISHED || status == MatchStatus.WALKOVER;
    }
    
    public boolean hasWinner() {
        return winner != null;
    }
    
    public void setWinnerFromScore() {
        if (team1Score != null && team2Score != null) {
            if (team1Score > team2Score) {
                this.winner = team1;
            } else if (team2Score > team1Score) {
                this.winner = team2;
            }
            // Se empate, winner permanece null (pode implementar pênaltis depois)
        }
    }
}
