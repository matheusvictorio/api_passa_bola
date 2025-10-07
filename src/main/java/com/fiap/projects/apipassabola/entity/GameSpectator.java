package com.fiap.projects.apipassabola.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing spectator participation in games
 * Spectators can subscribe to watch FRIENDLY and CHAMPIONSHIP games
 */
@Entity
@Table(name = "game_spectators", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"game_id", "spectator_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSpectator {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;
    
    @ManyToOne
    @JoinColumn(name = "spectator_id", nullable = false)
    private Spectator spectator;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpectatorStatus status = SpectatorStatus.CONFIRMED;
    
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum SpectatorStatus {
        CONFIRMED,
        CANCELLED,
        NO_SHOW
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public boolean isConfirmed() {
        return status == SpectatorStatus.CONFIRMED;
    }
    
    public boolean isCancelled() {
        return status == SpectatorStatus.CANCELLED;
    }
}
