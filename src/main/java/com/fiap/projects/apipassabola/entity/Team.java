package com.fiap.projects.apipassabola.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"players"})
public class Team {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name_team", nullable = false)
    private String nameTeam;
    
    @ManyToOne
    @JoinColumn(name = "leader_id", nullable = false)
    private Player leader;
    
    @ManyToMany(mappedBy = "teams")
    private Set<Player> players = new HashSet<>();
    
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
    
    // Helper methods
    public int getPlayersCount() {
        return players != null ? players.size() : 0;
    }
    
    public boolean isLeader(Player player) {
        return leader != null && leader.getId().equals(player.getId());
    }
    
    public boolean hasPlayer(Player player) {
        return players != null && players.stream()
                .anyMatch(p -> p.getId().equals(player.getId()));
    }
    
    public void addPlayer(Player player) {
        if (players == null) {
            players = new HashSet<>();
        }
        players.add(player);
        if (player.getTeams() == null) {
            player.setTeams(new HashSet<>());
        }
        player.getTeams().add(this);
    }
    
    public void removePlayer(Player player) {
        if (players != null) {
            players.remove(player);
        }
        if (player.getTeams() != null) {
            player.getTeams().remove(this);
        }
    }
}
