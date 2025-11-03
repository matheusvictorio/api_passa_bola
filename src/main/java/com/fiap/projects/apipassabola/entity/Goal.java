package com.fiap.projects.apipassabola.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade para registrar gols marcados em jogos
 */
@Entity
@Table(name = "goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Goal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;
    
    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;
    
    @Column(name = "team_side", nullable = false)
    private Integer teamSide; // 1 para time 1, 2 para time 2
    
    @Column(name = "minute")
    private Integer minute; // Minuto do gol (opcional)
    
    @Column(name = "is_own_goal")
    private Boolean isOwnGoal = false; // Gol contra
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
