package com.fiap.projects.apipassabola.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
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
public class Organization implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
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
        return followers != null ? followers.size() : 0;
    }
    
    public int getFollowingCount() {
        return following != null ? following.size() : 0;
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
