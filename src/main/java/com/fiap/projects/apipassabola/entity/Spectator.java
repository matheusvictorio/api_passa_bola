package com.fiap.projects.apipassabola.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
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
public class Spectator implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
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
