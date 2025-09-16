package com.fiap.projects.apipassabola.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSummaryResponse {
    
    private Long id;
    private String username;
    private String name;
    private String email;
    private String bio;
    private String profilePhotoUrl;
    private int followersCount;
    private int followingCount;
    private Integer gamesPlayed;
    private OrganizationSummaryResponse organization;
}
