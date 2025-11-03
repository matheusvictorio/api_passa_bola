package com.fiap.projects.apipassabola.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing watcher participation in games
 * Both Players and Spectators can subscribe to watch FRIENDLY and CHAMPIONSHIP games
 */
@Entity
@Table(name = "game_spectators", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"game_id", "watcher_id", "watcher_type"})
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
    
    // Universal watcher fields - can be Player or Spectator
    @Column(name = "watcher_id", nullable = false)
    private Long watcherId;
    
    @Column(name = "watcher_username", nullable = false)
    private String watcherUsername;
    
    @Column(name = "watcher_name", nullable = false)
    private String watcherName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "watcher_type", nullable = false)
    private UserType watcherType;
    
    // Legacy field for backward compatibility - marked as nullable
    @ManyToOne
    @JoinColumn(name = "spectator_id")
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
    
    // Helper method to check if watcher belongs to a user
    public boolean belongsTo(Long userId, UserType userType) {
        return this.watcherId.equals(userId) && this.watcherType == userType;
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
