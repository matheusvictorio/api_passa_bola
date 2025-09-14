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
public class SpectatorResponse {
    
    private Long id;
    private UserType userType;
    private String username;
    private String name;
    private String email;
    private String bio;
    private LocalDate birthDate;
    private String phone;
    private String profilePhotoUrl;
    private String bannerUrl;
    private Long favoriteTeamId;
    private OrganizationSummaryResponse favoriteTeam;
    
    // Collections
    private int followersCount;
    private int followingCount;
    private Set<GameSummaryResponse> subscribedGames;
    private Set<PostSummaryResponse> posts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
