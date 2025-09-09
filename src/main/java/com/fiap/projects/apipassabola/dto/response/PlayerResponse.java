package com.fiap.projects.apipassabola.dto.response;

import com.fiap.projects.apipassabola.entity.Player;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerResponse {
    
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String bio;
    private LocalDate birthDate;
    private Player.Position position;
    private String profilePhotoUrl;
    private Integer jerseyNumber;
    private OrganizationSummaryResponse organization;
    private int followersCount;
    private int followingCount;
    private int postsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
