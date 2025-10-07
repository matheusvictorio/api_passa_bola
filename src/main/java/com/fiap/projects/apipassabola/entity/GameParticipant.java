package com.fiap.projects.apipassabola.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing individual player and spectator participation in FRIENDLY and CHAMPIONSHIP games
 * Players participate in teams (side 1 or 2), spectators just watch
 */
@Entity
@Table(name = "game_participants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameParticipant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;
    
    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;
    
    @ManyToOne
    @JoinColumn(name = "spectator_id")
    private Spectator spectator;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType; // PLAYER or SPECTATOR
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipationType participationType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipationStatus status = ParticipationStatus.CONFIRMED;
    
    @Column(name = "team_side")
    private Integer teamSide; // 1 for team 1, 2 for team 2
    
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum ParticipationType {
        INDIVIDUAL,  // Player joined individually
        WITH_TEAM,   // Player joined with their team
        SPECTATOR    // Spectator watching the game
    }
    
    public enum ParticipationStatus {
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
    public boolean isTeamSide1() {
        return teamSide != null && teamSide == 1;
    }
    
    public boolean isTeamSide2() {
        return teamSide != null && teamSide == 2;
    }
    
    public boolean isIndividualParticipation() {
        return participationType == ParticipationType.INDIVIDUAL;
    }
    
    public boolean isTeamParticipation() {
        return participationType == ParticipationType.WITH_TEAM;
    }
    
    public boolean isConfirmed() {
        return status == ParticipationStatus.CONFIRMED;
    }
    
    public boolean isSpectator() {
        return participationType == ParticipationType.SPECTATOR;
    }
    
    public boolean isPlayer() {
        return participationType == ParticipationType.INDIVIDUAL || participationType == ParticipationType.WITH_TEAM;
    }
    
    public Long getUserId() {
        if (userType == UserType.PLAYER && player != null) {
            return player.getId();
        } else if (userType == UserType.SPECTATOR && spectator != null) {
            return spectator.getId();
        }
        return null;
    }
    
    public String getUserName() {
        if (userType == UserType.PLAYER && player != null) {
            return player.getName();
        } else if (userType == UserType.SPECTATOR && spectator != null) {
            return spectator.getName();
        }
        return null;
    }
}
