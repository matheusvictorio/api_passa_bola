package com.fiap.projects.apipassabola.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing team invitations for CUP games
 */
@Entity
@Table(name = "game_invites")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameInvite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;
    
    @ManyToOne
    @JoinColumn(name = "inviting_organization_id", nullable = false)
    private Organization invitingOrganization;
    
    @ManyToOne
    @JoinColumn(name = "invited_team_id", nullable = false)
    private Team invitedTeam;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status = InviteStatus.PENDING;
    
    @Column(name = "team_position") // "HOME" or "AWAY"
    private String teamPosition;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "invited_at")
    private LocalDateTime invitedAt;
    
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum InviteStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        CANCELLED,
        EXPIRED
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (invitedAt == null) {
            invitedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public boolean isPending() {
        return status == InviteStatus.PENDING;
    }
    
    public boolean isAccepted() {
        return status == InviteStatus.ACCEPTED;
    }
    
    public boolean isRejected() {
        return status == InviteStatus.REJECTED;
    }
    
    public boolean isCancelled() {
        return status == InviteStatus.CANCELLED;
    }
    
    public boolean isExpired() {
        return status == InviteStatus.EXPIRED;
    }
    
    public boolean isHomeTeam() {
        return "HOME".equalsIgnoreCase(teamPosition);
    }
    
    public boolean isAwayTeam() {
        return "AWAY".equalsIgnoreCase(teamPosition);
    }
    
    public boolean canBeAccepted() {
        return status == InviteStatus.PENDING && 
               game.getGameDate().isAfter(LocalDateTime.now());
    }
    
    public boolean canBeCancelled() {
        return status == InviteStatus.PENDING;
    }
    
    public void accept() {
        if (canBeAccepted()) {
            this.status = InviteStatus.ACCEPTED;
            this.respondedAt = LocalDateTime.now();
        }
    }
    
    public void reject() {
        if (status == InviteStatus.PENDING) {
            this.status = InviteStatus.REJECTED;
            this.respondedAt = LocalDateTime.now();
        }
    }
    
    public void cancel() {
        if (canBeCancelled()) {
            this.status = InviteStatus.CANCELLED;
            this.respondedAt = LocalDateTime.now();
        }
    }
}
