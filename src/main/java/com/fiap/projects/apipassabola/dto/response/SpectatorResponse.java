package com.fiap.projects.apipassabola.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpectatorResponse {
    
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String bio;
    private LocalDate birthDate;
    private String profilePhotoUrl;
    private Long favoriteTeamId;
    private OrganizationSummaryResponse favoriteTeam;
    private int followedPlayersCount;
    private int followedOrganizationsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
