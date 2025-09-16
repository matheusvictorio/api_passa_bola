package com.fiap.projects.apipassabola.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_invites")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamInvite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
    
    @ManyToOne
    @JoinColumn(name = "inviter_id", nullable = false)
    private Player inviter; // Quem enviou o convite (deve ser a leader)
    
    @ManyToOne
    @JoinColumn(name = "invited_player_id", nullable = false)
    private Player invitedPlayer; // Jogadora convidada
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status = InviteStatus.PENDING;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum InviteStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        CANCELLED
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
}
