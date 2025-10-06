package com.fiap.projects.apipassabola.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "players")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"teams", "followers", "following", "favoriteOrganizations", "subscribedGames", "posts", "createdGames"})
public class Player implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Global unique user ID across all user types
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;
    
    // User fields flattened
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType = UserType.PLAYER;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    private String bio;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;
    
    @Column(name = "banner_url")
    private String bannerUrl;
    
    private String phone;
    
    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;
    
    @Column(name = "past_organization")
    private String pastOrganization;
    
    @Column(name = "games_played")
    private Integer gamesPlayed = 0;
    
    // Teams relationship - Player can be in multiple teams
    @ManyToMany
    @JoinTable(
        name = "team_players",
        joinColumns = @JoinColumn(name = "player_id"),
        inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    private Set<Team> teams = new HashSet<>();
    
    // Posts created by this player
    // Note: This is a derived relationship, not stored directly
    @Transient
    private Set<Post> posts = new HashSet<>();
    
    @ManyToMany
    @JoinTable(
        name = "player_followers",
        joinColumns = @JoinColumn(name = "followed_id"),
        inverseJoinColumns = @JoinColumn(name = "follower_id")
    )
    private Set<Player> followers = new HashSet<>();
    
    @ManyToMany(mappedBy = "followers")
    private Set<Player> following = new HashSet<>();
    
    @ManyToMany
    @JoinTable(
        name = "player_favorite_organizations",
        joinColumns = @JoinColumn(name = "player_id"),
        inverseJoinColumns = @JoinColumn(name = "organization_id")
    )
    private Set<Organization> favoriteOrganizations = new HashSet<>();
    
    // Cross-type followers: Spectators following this Player
    @ManyToMany(mappedBy = "followingPlayers")
    private Set<Spectator> spectatorFollowers = new HashSet<>();
    
    // Cross-type following: Player following Spectators
    @ManyToMany
    @JoinTable(
        name = "player_following_spectators",
        joinColumns = @JoinColumn(name = "player_id"),
        inverseJoinColumns = @JoinColumn(name = "spectator_id")
    )
    private Set<Spectator> followingSpectators = new HashSet<>();
    
    // Cross-type following: Player following Organizations (beyond favorites)
    @ManyToMany
    @JoinTable(
        name = "player_following_organizations",
        joinColumns = @JoinColumn(name = "player_id"),
        inverseJoinColumns = @JoinColumn(name = "organization_id")
    )
    private Set<Organization> followingOrganizations = new HashSet<>();
    
    // Cross-type followers: Organizations following this Player
    @ManyToMany(mappedBy = "followingPlayers")
    private Set<Organization> organizationFollowers = new HashSet<>();
    
    // Games where this player's organization participates
    // Note: This is a derived relationship, not stored directly
    @Transient
    private Set<Game> createdGames = new HashSet<>();
    
    @ManyToMany
    @JoinTable(
        name = "player_subscribed_games",
        joinColumns = @JoinColumn(name = "player_id"),
        inverseJoinColumns = @JoinColumn(name = "game_id")
    )
    private Set<Game> subscribedGames = new HashSet<>();
    
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
    public int getFollowersCount() {
        int count = 0;
        // Count all types of followers
        count += (followers != null ? followers.size() : 0);  // Players following
        count += (spectatorFollowers != null ? spectatorFollowers.size() : 0);  // Spectators following
        count += (organizationFollowers != null ? organizationFollowers.size() : 0);  // Organizations following
        return count;
    }
    
    public int getFollowingCount() {
        int count = 0;
        // Count all types being followed
        count += (following != null ? following.size() : 0);  // Players being followed
        count += (followingSpectators != null ? followingSpectators.size() : 0);  // Spectators being followed
        count += (followingOrganizations != null ? followingOrganizations.size() : 0);  // Organizations being followed
        return count;
    }
    
    // Method to get the real username field (not email)
    public String getRealUsername() {
        return username;
    }
    
    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + userType.name()));
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return email; // Return email for Spring Security authentication
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}
