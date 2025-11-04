package com.fiap.projects.apipassabola.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tournaments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tournament {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "game_type", nullable = false)
    private GameType gameType; // CUP (obrigatório) ou CHAMPIONSHIP (opcional)
    
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;
    
    @Column(name = "creator_username")
    private String creatorUsername;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TournamentStatus status = TournamentStatus.REGISTRATION;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String venue;
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "total_teams", nullable = false)
    private Integer totalTeams = 0; // Número de times inscritos
    
    @Column(name = "max_teams")
    private Integer maxTeams; // Limite de times (4, 8, 16, 32, etc.)
    
    @Column(name = "current_round")
    private String currentRound; // "FINAL", "SEMI", "QUARTER", "ROUND_OF_16", etc.
    
    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TournamentTeam> teams = new ArrayList<>();
    
    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TournamentMatch> matches = new ArrayList<>();
    
    @Column(name = "bracket_generated")
    private Boolean bracketGenerated = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum TournamentStatus {
        REGISTRATION,  // Período de inscrição de times
        BRACKET_READY, // Chaveamento gerado, aguardando início
        IN_PROGRESS,   // Torneio em andamento
        FINISHED,      // Torneio finalizado
        CANCELLED      // Torneio cancelado
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
    public boolean canGenerateBracket() {
        return status == TournamentStatus.REGISTRATION 
            && !bracketGenerated 
            && totalTeams >= 2 
            && isPowerOfTwo(totalTeams);
    }
    
    public boolean isRegistrationOpen() {
        return status == TournamentStatus.REGISTRATION;
    }
    
    public boolean isBracketGenerated() {
        return bracketGenerated != null && bracketGenerated;
    }
    
    public boolean isCupTournament() {
        return gameType == GameType.CUP;
    }
    
    public boolean isChampionshipTournament() {
        return gameType == GameType.CHAMPIONSHIP;
    }
    
    private boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }
    
    public int getNextPowerOfTwo() {
        int power = 2;
        while (power < totalTeams) {
            power *= 2;
        }
        return power;
    }
    
    public String getRoundName(int teamsInRound) {
        return switch (teamsInRound) {
            case 2 -> "FINAL";
            case 4 -> "SEMI";
            case 8 -> "QUARTER";
            case 16 -> "ROUND_OF_16";
            case 32 -> "ROUND_OF_32";
            default -> "ROUND_OF_" + teamsInRound;
        };
    }
}
