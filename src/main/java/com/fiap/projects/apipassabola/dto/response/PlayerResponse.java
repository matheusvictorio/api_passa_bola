package com.fiap.projects.apipassabola.dto.response;

import com.fiap.projects.apipassabola.entity.UserType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerResponse {
    
    private Long id;  // Entity ID (sequential)
    private String userId;  // Global unique user ID
    private UserType userType;
    private String username;
    private String name;
    private String email;
    private String bio;
    private LocalDate birthDate;
    private String profilePhotoUrl;
    private String bannerUrl;
    private String phone;
    private OrganizationSummaryResponse organization;
    private String pastOrganization;
    private Integer gamesPlayed;
    
    // Collections
    private int followersCount;
    private int followingCount;
    private Set<OrganizationSummaryResponse> teams;
    private Set<GameSummaryResponse> createdGames;
    private Set<GameSummaryResponse> subscribedGames;
    private Set<PostSummaryResponse> posts;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
