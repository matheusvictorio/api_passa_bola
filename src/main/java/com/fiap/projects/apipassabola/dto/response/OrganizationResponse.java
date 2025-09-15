package com.fiap.projects.apipassabola.dto.response;

import com.fiap.projects.apipassabola.entity.UserType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {
    
    private Long id;
    private UserType userType;
    private String username;
    private String name;
    private String email;
    private String cnpj;
    private String bio;
    private String profilePhotoUrl;
    private String bannerUrl;
    private String phone;
    private String city;
    private String state;
    private Integer gamesPlayed;
    
    // Collections
    private int followersCount;
    private int followingCount;
    private Set<PlayerSummaryResponse> teams;
    private Set<GameSummaryResponse> createdGames;
    private Set<GameSummaryResponse> subscribedGames;
    private Set<PostSummaryResponse> posts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
