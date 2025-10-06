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
@Table(name = "spectators")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {
    "followers",
    "following",
    "followingPlayers",
    "followingOrganizations",
    "playerFollowers",
    "organizationFollowers",
    "subscribedGames",
    "posts"
})
public class Spectator implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Global unique user ID across all user types
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;
    
    // User fields flattened
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType = UserType.SPECTATOR;
    
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
    
    private String phone;
    
    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;
    
    @Column(name = "banner_url")
    private String bannerUrl;
    
    @ManyToOne
    @JoinColumn(name = "favorite_team_id")
    private Organization favoriteTeam;
    
    
    // Spectator-to-Spectator following
    @ManyToMany
    @JoinTable(
        name = "spectator_followers",
        joinColumns = @JoinColumn(name = "followed_id"),
        inverseJoinColumns = @JoinColumn(name = "follower_id")
    )
    private Set<Spectator> followers = new HashSet<>();
    
    @ManyToMany(mappedBy = "followers")
    private Set<Spectator> following = new HashSet<>();
    
    // Cross-type following: Spectator following Players
    @ManyToMany
    @JoinTable(
        name = "spectator_following_players",
        joinColumns = @JoinColumn(name = "spectator_id"),
        inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private Set<Player> followingPlayers = new HashSet<>();
    
    // Cross-type following: Spectator following Organizations
    @ManyToMany
    @JoinTable(
        name = "spectator_following_organizations",
        joinColumns = @JoinColumn(name = "spectator_id"),
        inverseJoinColumns = @JoinColumn(name = "organization_id")
    )
    private Set<Organization> followingOrganizations = new HashSet<>();
    
    // Cross-type followers: Players following this Spectator
    @ManyToMany(mappedBy = "followingSpectators")
    private Set<Player> playerFollowers = new HashSet<>();
    
    // Cross-type followers: Organizations following this Spectator
    @ManyToMany(mappedBy = "followingSpectators")
    private Set<Organization> organizationFollowers = new HashSet<>();
    
    @ManyToMany
    @JoinTable(
        name = "spectator_subscribed_games",
        joinColumns = @JoinColumn(name = "spectator_id"),
        inverseJoinColumns = @JoinColumn(name = "game_id")
    )
    private Set<Game> subscribedGames = new HashSet<>();
    
    // Posts created by this spectator
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
        count += (followers != null ? followers.size() : 0);  // Spectators following
        count += (playerFollowers != null ? playerFollowers.size() : 0);  // Players following
        count += (organizationFollowers != null ? organizationFollowers.size() : 0);  // Organizations following
        return count;
    }
    
    public int getFollowingCount() {
        int count = 0;
        // Count all types being followed
        count += (following != null ? following.size() : 0);  // Spectators being followed
        count += (followingPlayers != null ? followingPlayers.size() : 0);  // Players being followed
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
