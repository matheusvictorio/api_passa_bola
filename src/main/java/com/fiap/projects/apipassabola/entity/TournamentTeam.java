package com.fiap.projects.apipassabola.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tournament_teams", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tournament_id", "team_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentTeam {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;
    
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
    
    @Column(name = "seed_position")
    private Integer seedPosition; // Posição no chaveamento (1, 2, 3, 4...)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamStatus status = TeamStatus.REGISTERED;
    
    @Column(name = "registered_at")
    private LocalDateTime registeredAt;
    
    public enum TeamStatus {
        REGISTERED,  // Time inscrito
        CONFIRMED,   // Inscrição confirmada
        ELIMINATED,  // Eliminado do torneio
        CHAMPION,    // Campeão
        RUNNER_UP,   // Vice-campeão
        WITHDRAWN    // Desistiu
    }
    
    @PrePersist
    protected void onCreate() {
        registeredAt = LocalDateTime.now();
    }
}
