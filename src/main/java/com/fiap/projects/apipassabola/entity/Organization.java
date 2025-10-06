package com.fiap.projects.apipassabola.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "organizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {
    "followers",
    "following",
    "favoritedByPlayers",
    "spectatorFollowers",
    "playerFollowers",
    "followingPlayers",
    "followingSpectators",
    "createdGames",
    "subscribedGames",
    "posts"
})
public class Organization implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Global unique user ID across all user types
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;
    
    // User fields flattened
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType = UserType.ORGANIZATION;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(name = "cnpj", unique = true, nullable = false, length = 14)
    private String cnpj;
    
    @Column(nullable = false)
    private String password;
    
    private String bio;
    
    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;
    
    @Column(name = "banner_url")
    private String bannerUrl;
    
    private String phone;
    
    private String city;
    
    private String state;
    
    @Column(name = "games_played")
    private Integer gamesPlayed = 0;
    
    
    @ManyToMany
    @JoinTable(
        name = "organization_followers",
        joinColumns = @JoinColumn(name = "followed_id"),
        inverseJoinColumns = @JoinColumn(name = "follower_id")
    )
    private Set<Organization> followers = new HashSet<>();
    
    @ManyToMany(mappedBy = "followers")
    private Set<Organization> following = new HashSet<>();
    
    @ManyToMany(mappedBy = "favoriteOrganizations")
    private Set<Player> favoritedByPlayers = new HashSet<>();
    
    // Cross-type followers: Spectators following this Organization
    @ManyToMany(mappedBy = "followingOrganizations")
    private Set<Spectator> spectatorFollowers = new HashSet<>();
    
    // Cross-type followers: Players following this Organization
    @ManyToMany(mappedBy = "followingOrganizations")
    private Set<Player> playerFollowers = new HashSet<>();
    
    // Cross-type following: Organization following Players
    @ManyToMany
    @JoinTable(
        name = "organization_following_players",
        joinColumns = @JoinColumn(name = "organization_id"),
        inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private Set<Player> followingPlayers = new HashSet<>();
    
    // Cross-type following: Organization following Spectators
    @ManyToMany
    @JoinTable(
        name = "organization_following_spectators",
        joinColumns = @JoinColumn(name = "organization_id"),
        inverseJoinColumns = @JoinColumn(name = "spectator_id")
    )
    private Set<Spectator> followingSpectators = new HashSet<>();
    
    // Games where this organization is either home or away team
    // Note: This is a derived relationship, not stored directly
    @Transient
    private Set<Game> createdGames = new HashSet<>();
    
    @ManyToMany
    @JoinTable(
        name = "organization_subscribed_games",
        joinColumns = @JoinColumn(name = "organization_id"),
        inverseJoinColumns = @JoinColumn(name = "game_id")
    )
    private Set<Game> subscribedGames = new HashSet<>();
    
    // Posts created by this organization
    // Note: This is a derived relationship, not stored directly
    @Transient
    private Set<Post> posts = new HashSet<>();
    
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
        count += (followers != null ? followers.size() : 0);  // Organizations following
        count += (playerFollowers != null ? playerFollowers.size() : 0);  // Players following
        count += (spectatorFollowers != null ? spectatorFollowers.size() : 0);  // Spectators following
        return count;
    }
    
    public int getFollowingCount() {
        int count = 0;
        // Count all types being followed
        count += (following != null ? following.size() : 0);  // Organizations being followed
        count += (followingPlayers != null ? followingPlayers.size() : 0);  // Players being followed
        count += (followingSpectators != null ? followingSpectators.size() : 0);  // Spectators being followed
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
