package com.fiap.projects.apipassabola.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "spectators")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Spectator {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    private String bio;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;
    
    @ManyToOne
    @JoinColumn(name = "favorite_team_id")
    private Organization favoriteTeam;
    
    @ManyToMany
    @JoinTable(
        name = "spectator_followed_players",
        joinColumns = @JoinColumn(name = "spectator_id"),
        inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private Set<Player> followedPlayers = new HashSet<>();
    
    @ManyToMany
    @JoinTable(
        name = "spectator_followed_organizations",
        joinColumns = @JoinColumn(name = "spectator_id"),
        inverseJoinColumns = @JoinColumn(name = "organization_id")
    )
    private Set<Organization> followedOrganizations = new HashSet<>();
    
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
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public int getFollowedPlayersCount() {
        return followedPlayers != null ? followedPlayers.size() : 0;
    }
    
    public int getFollowedOrganizationsCount() {
        return followedOrganizations != null ? followedOrganizations.size() : 0;
    }
}
